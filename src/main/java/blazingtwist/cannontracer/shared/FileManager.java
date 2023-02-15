package blazingtwist.cannontracer.shared;

import blazingtwist.cannontracer.CannonTracerMod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FilenameUtils;

public class FileManager {

	public static final String traceDirectoryPrefix = "traces/";
	public static final String traceFileExtension = ".trace";

	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES,
					MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS,
					MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
			.disable(MapperFeature.AUTO_DETECT_CREATORS,
					MapperFeature.AUTO_DETECT_FIELDS,
					MapperFeature.AUTO_DETECT_GETTERS,
					MapperFeature.AUTO_DETECT_IS_GETTERS)
			.build();

	private static final Pattern specialFileChars = Pattern.compile("[\\\\/.]]");

	public static Path resolveSubPath(String subPath) {
		return FabricLoader.getInstance().getConfigDir().resolve(subPath);
	}

	public static List<String> listSubFiles(String subPath) {
		URI baseUri = FabricLoader.getInstance().getConfigDir().resolve(traceDirectoryPrefix).toUri();
		try (Stream<Path> stream = Files.walk(FabricLoader.getInstance().getConfigDir().resolve(subPath))) {
			return stream
					.filter(file -> file.toFile().isFile())
					.map(file -> {
						String relativeParentPath = baseUri.relativize(file.getParent().toUri()).getPath();
						return relativeParentPath + FilenameUtils.getBaseName(file.toFile().getName());
					})
					.collect(Collectors.toList());
		} catch (IOException e) {
			CannonTracerMod.LOGGER.error("File-Walking failed! subPath: '{}'", subPath, e);
			return null;
		}
	}

	public static boolean containsSpecialChars(String str) {
		return specialFileChars.matcher(str).find();
	}

	public static boolean fileExists(String subPath) {
		Path targetPath = FabricLoader.getInstance().getConfigDir().resolve(subPath);
		File file = targetPath.toFile();
		return file.exists() && file.isFile();
	}

	public static String readFileToString(String subPath) {
		Path targetPath = FabricLoader.getInstance().getConfigDir().resolve(subPath);
		if (!fileExists(subPath)) {
			return null;
		}

		try {
			return Files.readString(targetPath);
		} catch (IOException e) {
			CannonTracerMod.LOGGER.error("reading file to string failed! subPath: '{}'", subPath, e);
			return null;
		}
	}

	public static boolean saveToFile(String subPath, String content) {
		Path targetPath = FabricLoader.getInstance().getConfigDir().resolve(subPath);
		File targetFile = targetPath.toFile();
		File targetDirectoryFile = targetFile.getParentFile();
		if (!targetDirectoryFile.exists() || !targetDirectoryFile.isDirectory()) {
			if (!targetDirectoryFile.mkdirs()) {
				CannonTracerMod.LOGGER.error("saveToFile mkDirs failed! subPath: '{}'", subPath);
				return false;
			}
		}

		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(targetFile, false), StandardCharsets.UTF_8);
			writer.write(content);
			writer.close();
			return true;
		} catch (IOException e) {
			CannonTracerMod.LOGGER.error("saveToFile failed! subPath: '{}'", subPath, e);
			return false;
		}
	}

	public static boolean deleteFile(String subPath) {
		Path targetPath = FabricLoader.getInstance().getConfigDir().resolve(subPath);
		File targetFile = targetPath.toFile();
		if (!targetFile.exists() || targetFile.isDirectory()) {
			return false;
		}
		return targetFile.delete();
	}

	public static String objectToJson(Object data) {
		try {
			return OBJECT_MAPPER.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			CannonTracerMod.LOGGER.error("object serialization failed!", e);
			return null;
		}
	}

	public static <T> T parseJsonToObject(String json, TypeReference<T> typeRef) throws JsonProcessingException {
		return OBJECT_MAPPER.readValue(json, typeRef);
	}

	public static <T> T tryDeserializeToObject(String subPath, TypeReference<T> typeRef, Supplier<T> defaultProvider) {
		if (!fileExists(subPath)) {
			return defaultProvider.get();
		}

		String json = readFileToString(subPath);
		if (json == null) {
			return defaultProvider.get();
		}

		try {
			return FileManager.parseJsonToObject(json, typeRef);
		} catch (JsonProcessingException e) {
			CannonTracerMod.LOGGER.error("failed to deserialize from subPath: '{}'", subPath, e);
			return defaultProvider.get();
		}
	}

}

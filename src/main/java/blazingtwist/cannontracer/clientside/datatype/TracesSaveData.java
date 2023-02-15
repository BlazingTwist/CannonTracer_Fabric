package blazingtwist.cannontracer.clientside.datatype;

import blazingtwist.cannontracer.clientside.TraceRenderer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TracesSaveData {

	@JsonProperty("saveData")
	private final Map<String, EntityTracesSaveData> saveData = new HashMap<>();

	public TracesSaveData() {
	}

	public TracesSaveData(HashMap<String, HashMap<TraceRenderer.TracePos, TraceRenderer.TickMetaData>> traceData) {
		for (Map.Entry<String, HashMap<TraceRenderer.TracePos, TraceRenderer.TickMetaData>> entry : traceData.entrySet()) {
			saveData.put(entry.getKey(), new EntityTracesSaveData(entry.getValue()));
		}
	}

	public Map<String, EntityTracesSaveData> getSaveData() {
		return saveData;
	}

	public static class EntityTracesSaveData {

		@JsonProperty("posList")
		private final List<TraceRenderer.TracePos> posList = new ArrayList<>();

		@JsonProperty("metaDataList")
		private final List<TraceRenderer.TickMetaData> metaDataList = new ArrayList<>();

		public EntityTracesSaveData() {
		}

		public EntityTracesSaveData(HashMap<TraceRenderer.TracePos, TraceRenderer.TickMetaData> data) {
			for (Map.Entry<TraceRenderer.TracePos, TraceRenderer.TickMetaData> entry : data.entrySet()) {
				posList.add(entry.getKey());
				metaDataList.add(entry.getValue());
			}
		}

		public void loadToTraceRenderer(HashMap<TraceRenderer.TracePos, TraceRenderer.TickMetaData> rendererData) {
			for (int i = 0; i < posList.size(); i++) {
				TraceRenderer.TickMetaData renderMeta = rendererData.computeIfAbsent(posList.get(i), x -> new TraceRenderer.TickMetaData());
				TraceRenderer.TickMetaData myMeta = metaDataList.get(i);
				renderMeta.merge(myMeta);
			}
		}
	}
}

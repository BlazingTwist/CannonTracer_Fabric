package blazingtwist.cannontracer.shared.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtils {

	private final MutableText result = Text.empty();
	private Formatting[] currentFormats = null;

	public TextUtils() {
	}

	/**
	 * Sets the formatting for the next message(s)
	 *
	 * @param formats varargs of Formatting rules to apply
	 * @return TextUtils instance for call chaining
	 */
	public TextUtils formatted(Formatting... formats) {
		currentFormats = formats;
		return this;
	}

	/**
	 * Append text to the end of this message
	 *
	 * @param text the text to append
	 * @return TextUtils instance for call chaining
	 */
	public TextUtils text(String text) {
		MutableText literal = Text.literal(text);
		if (currentFormats != null) {
			literal.formatted(currentFormats);
		}
		result.append(literal);
		return this;
	}

	/**
	 * @param delimiter    the string separating each entry (uses the currently active formatting)
	 * @param strings      a list of entries to write
	 * @param entryFormats formatting to use for entries
	 * @return TextUtils instance for call chaining
	 */
	public TextUtils list(String delimiter, Iterable<String> strings, Formatting... entryFormats) {
		MutableText delimiterText = currentFormats == null
				? Text.literal(delimiter)
				: Text.literal(delimiter).formatted(currentFormats);

		entryFormats = entryFormats.length == 0 ? null : entryFormats;

		boolean wroteFirstEntry = false;
		for (String entry : strings) {
			if (wroteFirstEntry) {
				result.append(delimiterText);
			}
			MutableText entryLiteral = Text.literal(entry);
			if (entryFormats != null) {
				entryLiteral.formatted(entryFormats);
			}
			result.append(entryLiteral);
			wroteFirstEntry = true;
		}
		return this;
	}

	/**
	 * Append a line-break to the end of this message
	 *
	 * @return TextUtils instance for call chaining
	 */
	public TextUtils lineBreak() {
		result.append("\n");
		return this;
	}

	public MutableText build() {
		return result;
	}
}

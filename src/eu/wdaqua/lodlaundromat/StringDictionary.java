package eu.wdaqua.lodlaundromat;

import java.io.File;
import java.io.IOException;

public class StringDictionary extends AbstractDictionary<String, String> {

	public StringDictionary(final File file) throws IOException {
		super(file);
	}

	@Override
	protected void addToDictonary(final String key, final String value) {
		this.dict.put(key, value);
	}

}

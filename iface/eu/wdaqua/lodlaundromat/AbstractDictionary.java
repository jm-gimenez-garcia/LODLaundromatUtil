package eu.wdaqua.lodlaundromat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public abstract class AbstractDictionary<K, V> {

	HashMap<K, V> dict = new HashMap<K, V>();

	public AbstractDictionary(final File file) throws IOException {
		super();
		String line;
		String[] fields;
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		while ((line = reader.readLine()) != null) {
			fields = line.split(",");
			addToDictonary(fields[0], fields[1]);
		}
		reader.close();
	}

	public V get(final K key) {
		return this.dict.get(key);
	}

	protected abstract void addToDictonary(String key, String value);

}

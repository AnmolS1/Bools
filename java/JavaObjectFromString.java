import java.io.IOException;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * this class helps in the conversion of a string to a file object
 */
class JavaObjectFromString extends SimpleJavaFileObject {
	private String contents = null;
	
	public JavaObjectFromString(String className, String contents) throws Exception {
		super(new URI(className), Kind.SOURCE);
		this.contents = contents;
	}
	
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return contents;
	}
}
package com.ensoftcorp.open.commons.utilities;

/**
 * Useful regular expressions for matching different naming styles.
 * 
 * Intended for use with CommonQueries.nodesMatchingRegex()
 * 
 * @author Tom Deering
 */
public class CodeStyles {

	private CodeStyles() {}

	// Identifies patterns which contain upper case characters
	public static final String CONTAINS_UPPER_CASE = "(.*)[A-Z](.*)";

	// Identifies patterns which contain lower case characters
	public static final String CONTAINS_LOWER_CASE = "(.*)[a-z](.*)";

	// Identifies patterns which contain a number
	public static final String CONTAINS_NUMBER = "(.*)[0-9](.*)";

	// Identifies patterns which contain an underscore
	public static final String CONTAINS_UNDERSCORE = "(.*)[_](.*)";

	// Identifies patterns which contain an underscore
	public static final String CONTAINS_DOT = "(.*)[\\.](.*)";

	// Identifies patterns which contain a dollar sign
	public static final String CONTAINS_DOLLAR_SIGN = "(.*)[$](.*)";

	// Identifies patterns which contain non-ascii characters
	public static final String CONTAINS_NON_ASCII = "(.*)[\\x00-\\x80](.*)";

	// Identifies patterns which begin with a lower case character
	public static final String BEGINS_LOWER_CASE = "[a-z](.*)";

	// Identifies patterns which begin with an upper case character
	public static final String BEGINS_CAPITAL = "[A-Z](.*)";

	// Identifies patterns like "myVariableName7"
	public static final String CAMEL = "^[a-z][a-zA-Z0-9]*$";

	// Identifies patterns like "MyVariableName7"
	public static final String CAMEL_HUMPED = "^[A-Z][a-zA-Z0-9]*$";

	// Identifies patterns like "MODE_WORLD_READABLE"
	public static final String UNDERSCORE_CAPITAL = "^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$";

	// Identifies patterns like "hello_there_world"
	public static final String UNDERSCORE_LOWER = "^[a-z][a-z0-9]*(_[a-z0-9]+)*$";

	// Identifies patterns like "Hello_THERE_world7""
	public static final String UNDERSCORE_MIXED = "^[a-zA-Z][a-zA-Z0-9]*(_[a-zA-Z0-9]+)*$";

	// Identifies patterns like "Q"
	public static final String CAPITAL_TYPE = "^[A-Z]$";

	// Identifies patterns like "com.android.internal_package7"
	public static final String PACKAGE_LOWER = "^[a-z]+(\\.[a-z_][a-z0-9_]*)*$";

	// Identifies patterns like "COM.ANDROID.PACKAGE"
	public static final String PACKAGE_UPPER = "^[A-Z]+(\\.[A-Z_][A-Z0-9_]*)*$";

	// Identifies patterns like "com.Android.InternalPackage_7"
	public static final String PACKAGE_MIXED = "^[a-zA-Z]+(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$";

}

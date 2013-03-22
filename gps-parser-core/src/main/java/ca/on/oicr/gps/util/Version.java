package ca.on.oicr.gps.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A small class to help implement semantic versioning. This is primarily used in handling
 * panels. We don't yet apply the semantics to the version, but we do rely on comparison 
 * when needed.
 * 
 * @author Stuart Watt
 *
 */
public class Version {
	
	private int major;
	private int minor;
	private int patch;
	
	/**
	 * Parser, which reads a string (typically beginning "v", but that is not required)
	 * followed by at least one integer, and parses it as a version string.
	 * @param versionString
	 */
	
	public static Version parseVersion(String versionString) {
		
		// Being nice, we trim the string - we don't need to, but we are good like that
		
		versionString = versionString.trim();
		Pattern p = Pattern.compile("v?(\\d+)(?:\\.(\\d+)){0,2}");
		Matcher m = p.matcher(versionString);
		
		if (m.matches()) {
			int major = Integer.parseInt(m.group(1));
			int minor = 0;
			int patch = 0;
			int groups = m.groupCount();
			if (groups >= 2) {
				minor = Integer.parseInt(m.group(2));
			}
			if (groups == 3) {
				patch = Integer.parseInt(m.group(3));
			}
			
			return new Version(major, minor, patch);
		} else {
			throw new NumberFormatException(versionString);
		}
	}
	
	/**
	 * Constructs a new version based in a major value, minor value, and patch value
	 * passed as parameters.
	 * @param major
	 * @param minor
	 * @param patch
	 */
	public Version(int major, int minor, int patch) {
		super();
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}
	
	/**
	 * Compares two different version values, returning -1 if the invocant is less than
	 * the argument, 1 if it is greater, and 0 if they are the same. 
	 * 
	 * @param version
	 * @return
	 */
	int compareTo(Version version) {
		if (major != version.major) {
			return (major < version.major) ? -1 : 1;
		} else if (minor != version.minor) {
			return (major < version.major) ? -1 : 1;
		} else if (patch != version.patch) {
			return (patch < version.patch) ? -1 : 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * Compares two different versions for equality.
	 * @param version
	 * @return
	 */
	boolean equals(Version version) {
		return major == version.major && 
		       minor == version.minor &&
		       patch == version.patch;
	}
	
	/**
	 * An extremely naive hash code, but it's unlikely you get many collisions in
	 * version numbering. 
	 * @return
	 */
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + major;
		hash = 31 * hash + minor;
		hash = 31 * hash + patch;
		return hash;
	}
	
	/**
	 * Stringifies a version, in a form which is suitable for parsing.
	 */
	@Override
	public String toString() {
		return "v" + major + "." + minor + "." + patch;
	}
	
	/**
	 * Returns the major component of the version
	 * @return
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * Returns the minor component of the version
	 * @return
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * Returns the patch component of the version.
	 * @return
	 */
	public int getPatch() {
		return patch;
	}
}

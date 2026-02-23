package org.giaquinto.kmrtd.lds.icao

import kmrtd.lds.icao.COMFile
import java.util.IllegalFormatConversionException
import java.util.StringTokenizer

/**
 * Constructs a new COM file.
 *
 * @param ldsVer a "x.y" version number
 * @param unicodeVer a "x.y.z" version number
 * @param tagList list of tags
 */
fun COMFile.Companion.from(
    ldsVer: String,
    unicodeVer: String,
    tagList: IntArray
): COMFile {
    try {
        var st = StringTokenizer(ldsVer, ".")
        require(st.countTokens() == 2) { "Could not parse LDS version. Expecting 2 level version number x.y." }
        val versionLDS = st.nextToken().trim { it <= ' ' }.toInt()
        val updateLevelLDS = st.nextToken().trim { it <= ' ' }.toInt()
        st = StringTokenizer(unicodeVer, ".")
        require(st.countTokens() == 3) { "Could not parse unicode version. Expecting 3 level version number x.y.z." }
        val majorVersionUnicode = st.nextToken().trim { it <= ' ' }.toInt()
        val minorVersionUnicode = st.nextToken().trim { it <= ' ' }.toInt()
        val releaseLevelUnicode = st.nextToken().trim { it <= ' ' }.toInt()
        return COMFile(
            String.format("%02d", versionLDS),
            String.format("%02d", updateLevelLDS),
            String.format("%02d", majorVersionUnicode),
            String.format("%02d", minorVersionUnicode),
            String.format("%02d", releaseLevelUnicode),
            tagList
        )
    }  catch (nfe: NumberFormatException) {
        throw IllegalArgumentException("Could not parse version number", nfe)
    } catch (ifce: IllegalFormatConversionException) {
        throw IllegalArgumentException("Could not parse version number", ifce)
    }
}
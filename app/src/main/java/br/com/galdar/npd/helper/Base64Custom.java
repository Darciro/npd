package br.com.galdar.npd.helper;

import android.util.Base64;

public class Base64Custom {

    public static String encodeBase64 ( String string ) {
        return Base64.encodeToString( string.getBytes(), Base64.DEFAULT ).replaceAll( "(\\n|\\r)", "" );
    }

    public static String decodeBase64 ( String string ) {
        return new String( Base64.decode( string, Base64.DEFAULT ) );
    }
}

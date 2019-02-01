package br.com.galdar.npd.helper;

import java.text.SimpleDateFormat;

public class DateCustom {

    public static String currentDateFormated () {
        long curDate = System.currentTimeMillis();
        // SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd/MM/yyyy hh:mm:ss" );
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd/MM/yyyy" );
        String dateString = simpleDateFormat.format( curDate );
        return dateString;
    }

    public static String dateFormatedToDatabase ( String date ) {

        String d[] = date.split("/");
        String monthYearFormat = d[1] + d[2];

        return monthYearFormat;
    }

}

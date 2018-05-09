package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Date in operation data.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationDateAttribute extends OperationDataAttribute {

    private Date date;

    /**
     * Default constructor.
     */
    public OperationDateAttribute() {
        this.type = Type.DATE;
    }

    /**
     * Constructor with date.
     * @param date Date.
     */
    public OperationDateAttribute(Date date) {
        this.date = date;
    }

    /**
     * Constructor with date as string.
     * @param date Date in format yyyy-MM-dd (month index starts by 1).
     */
    public OperationDateAttribute(String date) {
        if (date == null || !date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new IllegalArgumentException("Date is invalid: "+date+", expected format: yyyy-MM-dd.");
        }
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        this.date = calendar.getTime();
    }

    /**
     * Get date.
     * @return Date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set date.
     * @param date Date.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String formattedValue() {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return "D"+sdf.format(date);
    }
}

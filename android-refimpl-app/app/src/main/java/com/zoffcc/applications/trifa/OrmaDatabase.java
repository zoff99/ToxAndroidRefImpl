package com.zoffcc.applications.trifa;

public class OrmaDatabase extends com.zoffcc.applications.sorm.OrmaDatabase
{

    public OrmaDatabase getConnection()
    {
        return this;
    }

    public void execSQL(String s)
    {
        run_multi_sql(s);
    }
}

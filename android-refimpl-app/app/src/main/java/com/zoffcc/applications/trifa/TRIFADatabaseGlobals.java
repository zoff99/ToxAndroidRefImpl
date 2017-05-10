package com.zoffcc.applications.trifa;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class TRIFADatabaseGlobals
{
    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String key;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String value;
}

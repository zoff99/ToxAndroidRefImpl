package com.zoffcc.applications.sorm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class Generator {
    private static final String TAG = "Generator";
    static final String Version = "0.99.2";
    static final String prefix = "_sorm_";
    static final String tbl_f_ext = ".java";
    static final String tbl_s_ext = ".sql";
    static final String LOGGER_DB_PREFIX = "DB.";
    // on Android the logger tag length is limited
    static final int MAX_LOGGER_PRE_LEN = (20 - LOGGER_DB_PREFIX.length());
    static final String out_classdir = "com/zoffcc/applications/sorm/";
    static final String orma_global_in1 = "o1.txt";
    static final String orma_global_in2 = "o2.txt";
    static final String orma_global_t1 = "t1.txt";
    static final String orma_global_tbl01 = "tbl01.txt";
    static final String orma_global_tbl11 = "tbl11.txt";
    static final String orma_global_tbl21 = "tbl21.txt";
    static final String orma_global_tbl_tolist_01 = "tbl_tolist_01.txt";
    static final String orma_global_tbl_tolist_02 = "tbl_tolist_02.txt";
    static final String orma_global_tbl_insert_01 = "tbl_insert_01.txt";
    static final String orma_global_tbl_insert_02 = "tbl_insert_02.txt";
    static final String orma_global_tbl99 = "tbl99.txt";
    static final String orma_global_out = "OrmaDatabase.java";
    static String tbl_deepcopy = "";
    static String tbl_tostring = "";
    static String tbl_tolist = "";
    static int column_num = 0;
    static String primary_key_column_name = "";
    static String primary_key_column_autoincr_if_needed = "";
    static String primary_key_column_sqlitetype = "";
    static String tbl_insert = "";
    static String tbl_insert_sub01 = "";
    static String tbl_insert_sub02 = "";
    static String tbl_insert_sub03 = "";
    static String tbl_equalfuncs = "";
    static String tbl_orderbyfuncs = "";
    static String tbl_setfuncs = "";
    static final String java_quoted = "\\\"";

    enum COLTYPE
    {
        INT(1), LONG(2), STRING(3), BOOLEAN(4), UNKNOWN(999);
        private int value;
        private String name;
        private String javatype;
        private String sqlitetype;
        private COLTYPE(int value)
        {
            this.value = value;
            if (value == 1) { this.name = "INT"; };
            if (value == 2) { this.name = "LONG"; };
            if (value == 3) { this.name = "STRING"; };
            if (value == 4) { this.name = "BOOLEAN"; };
            if (value == 999) { this.name = "UNKNOWN"; };
            if (value == 1) { this.javatype = "int"; };
            if (value == 2) { this.javatype = "long"; };
            if (value == 3) { this.javatype = "String"; };
            if (value == 4) { this.javatype = "boolean"; };
            if (value == 999) { this.javatype = "Object"; };
            if (value == 1) { this.sqlitetype = "INTEGER"; };
            if (value == 2) { this.sqlitetype = "INTEGER"; };
            if (value == 3) { this.sqlitetype = "TEXT"; };
            if (value == 4) { this.sqlitetype = "BOOLEAN"; };
            if (value == 999) { this.sqlitetype = "TEXT"; };
        }
    }

    public static void main(String[] args) {
        System.out.println("Generator v" + Version);
        System.out.println("checking directory: " + args[0]);

        try
        {
            final String workdir = args[0];
            begin_orma(workdir, orma_global_out);

            // HINT: sort order does not seem to be exactly identical everywhere. this is the aweful "fix" with sorting
            File[] files = new File(workdir).listFiles();
            Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));

            for (final File fileEntry : files) {
                if (!fileEntry.isDirectory()) {
                    if (fileEntry.getName().startsWith(prefix))
                    {
                        // System.out.println(fileEntry.getName());
                        generate_table(workdir, fileEntry.getName(), fileEntry.getName().substring(prefix.length()));
                    }
                }
            }

            finish_orma(workdir, orma_global_out);

            String[] list = new String[]{"Column.java","Index.java","Log.java","Nullable.java","OnConflict.java","PrimaryKey.java","Table.java"};
            for (String i : list)
            {
                System.out.println("copying File: " + i);
                copy_file(new File(i), new File(workdir + File.separator + out_classdir + File.separator + i));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void copy_file(File sourceFile, File destFile)
    {
        if (!sourceFile.exists())
        {
            return;
        }
        try
        {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static String read_text_file(final String filename_with_path)
    {
        Path filePath = Path.of(filename_with_path);
            StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (Exception e)
        {
        }
        return contentBuilder.toString();
    }

    static void begin_orma(final String workdir, final String outfilename)
    {
        System.out.println("starting: " + workdir + File.separator + out_classdir + outfilename);
        try
        {
            File d = new File(workdir + File.separator + out_classdir);
            d.mkdirs();
            final String o1 = read_text_file(orma_global_in1);
            FileWriter fstream = new FileWriter(workdir + File.separator + out_classdir + outfilename,
                StandardCharsets.UTF_8);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(o1);
            out.newLine();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static void begin_table(final String workdir, final String tablename)
    {
        System.out.println("starting: " + workdir + File.separator + out_classdir + tablename + tbl_f_ext);
        tbl_deepcopy = "    public static "+tablename+" deep_copy("+tablename+" in)" + "\n";
        tbl_deepcopy += "    {" + "\n";
        tbl_deepcopy += "        "+tablename+" out = new "+tablename+"();" + "\n";

        tbl_tostring = "    @Override" + "\n";
        tbl_tostring += "    public String toString()" + "\n";
        tbl_tostring += "    {" + "\n";
        tbl_tostring += "        return ";

        tbl_tolist = read_text_file(orma_global_tbl_tolist_01).replace("__@@@TABLE@@@__", tablename);
        tbl_tolist += "                "+tablename+" out = new "+tablename+"();" + "\n";

        tbl_insert = read_text_file(orma_global_tbl_insert_01);
        tbl_insert_sub01 = "";
        tbl_insert_sub02 = "";
        tbl_insert_sub03 = "";
        tbl_equalfuncs =   "    // ----------------- Eq/Gt/Lt funcs ----------------- //" + "\n";
        tbl_orderbyfuncs = "    // ----------------- OrderBy funcs ------------------ //" + "\n";
        tbl_setfuncs =     "    // ----------------- Set funcs ---------------------- //" + "\n";
        column_num = 0;

        try
        {
            File d = new File(workdir + File.separator + out_classdir);
            d.mkdirs();
            FileWriter fstream = new FileWriter(workdir + File.separator + out_classdir + tablename + tbl_s_ext,
                StandardCharsets.UTF_8);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("CREATE TABLE IF NOT EXISTS \""+tablename+"\" (");
            out.newLine();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            File d = new File(workdir + File.separator + out_classdir);
            d.mkdirs();
            final String tbl01 = read_text_file(orma_global_tbl01);
            FileWriter fstream = new FileWriter(workdir + File.separator + out_classdir + tablename + tbl_f_ext,
                StandardCharsets.UTF_8);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(tbl01);
            out.newLine();
            out.write("@Table");
            out.newLine();
            out.write("public class " + tablename);
            out.newLine();
            out.write("{");
            out.newLine();
            out.write("    private static final String TAG = \"" + LOGGER_DB_PREFIX
                + tablename.substring(0, Math.min(tablename.length(), MAX_LOGGER_PRE_LEN)) + "\";");
            out.newLine();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static void finish_table(final String workdir, final String tablename)
    {
        System.out.println("finishing: " + workdir + File.separator + out_classdir + tablename + tbl_f_ext);
        try
        {
            File d = new File(workdir + File.separator + out_classdir);
            d.mkdirs();
            final String tbl99 = read_text_file(orma_global_tbl99);
            final String tbl11 = read_text_file(orma_global_tbl11);
            final String tbl21 = read_text_file(orma_global_tbl21);
            tbl_tolist += read_text_file(orma_global_tbl_tolist_02);
            tbl_insert += tbl_insert_sub01;
            tbl_insert += "                    + \")\" +" + "\n";
            tbl_insert += "                    \"values\" +" + "\n";
            tbl_insert += "                    \"(\"" + "\n";
            tbl_insert += tbl_insert_sub02;
            tbl_insert += "                    + \")\";" + "\n";
            tbl_insert += "" + "\n";
            tbl_insert += "            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);" + "\n";
            tbl_insert += "            insert_pstmt.clearParameters();" + "\n";
            tbl_insert += "" + "\n";
            tbl_insert += tbl_insert_sub03;
            tbl_insert += read_text_file(orma_global_tbl_insert_02);

            FileWriter fstream = new FileWriter(workdir + File.separator + out_classdir + tablename + tbl_f_ext,
                StandardCharsets.UTF_8, true); // append!
            BufferedWriter out = new BufferedWriter(fstream);
            tbl_deepcopy += "\n";
            tbl_deepcopy += "        return out;" + "\n";
            tbl_deepcopy += "    }" + "\n";
            tbl_deepcopy += "\n";
            out.write(tbl_deepcopy);

            tbl_tostring += ";" + "\n";
            tbl_tostring += "    }" + "\n";
            tbl_tostring += "" + "\n";
            out.write(tbl_tostring);

            out.newLine();
            out.write(tbl11);

            out.newLine();
            out.write(tbl_tolist);

            out.newLine();
            out.write(tbl_insert);

            out.newLine();
            out.write(tbl21.replace("__@@@TABLE@@@__", tablename));

            out.newLine();
            out.write(tbl_setfuncs);
            out.newLine();
            out.write(tbl_equalfuncs);
            out.newLine();
            out.write(tbl_orderbyfuncs);

            out.newLine();
            out.write(tbl99);
            out.newLine();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        append_to_sql(workdir, tablename, "  PRIMARY KEY(\""+primary_key_column_name+"\" "+primary_key_column_autoincr_if_needed+")");
        append_to_sql(workdir, tablename, ");");
    }

    static void append_to_table(final String workdir, final String tablename, final String txt_line)
    {
        // System.out.println("appending to table: " + workdir + File.separator + out_classdir + tablename + tbl_f_ext);
        try
        {
            File d = new File(workdir + File.separator + out_classdir);
            d.mkdirs();
            FileWriter fstream = new FileWriter(workdir + File.separator + out_classdir + tablename + tbl_f_ext,
                StandardCharsets.UTF_8, true); // append!
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(txt_line);
            out.newLine();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static void append_to_sql(final String workdir, final String tablename, final String txt_line)
    {
        // System.out.println("appending to table: " + workdir + File.separator + out_classdir + tablename + tbl_s_ext);
        try
        {
            File d = new File(workdir + File.separator + out_classdir);
            d.mkdirs();
            FileWriter fstream = new FileWriter(workdir + File.separator + out_classdir + tablename + tbl_s_ext,
                StandardCharsets.UTF_8, true); // append!
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(txt_line);
            out.newLine();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    static void finish_orma(final String workdir, final String outfilename)
    {
        System.out.println("finishing: " + workdir + File.separator + out_classdir + outfilename);
        try
        {
            File d = new File(workdir + File.separator + out_classdir);
            d.mkdirs();
            final String o2 = read_text_file(orma_global_in2);
            FileWriter fstream = new FileWriter(workdir + File.separator + out_classdir + outfilename,
                StandardCharsets.UTF_8, true); // append!
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(o2);
            out.newLine();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static void process_tablename(final String workdir, final String outfilename, final String tablename)
    {
        System.out.println("appending to orma: " + workdir + File.separator + out_classdir + outfilename);
        try
        {
            File d = new File(workdir + File.separator + out_classdir);
            d.mkdirs();
            final String t1 = read_text_file(orma_global_t1);
            FileWriter fstream = new FileWriter(workdir + File.separator + out_classdir + outfilename,
                StandardCharsets.UTF_8, true); // append!
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(t1.replace("__@@@TABLE@@@__", tablename));
            out.newLine();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        begin_table(workdir, tablename);
    }

    static void generate_table(final String workdir, final String infilename, final String outfilename)
    {
        System.out.println("generating: " + workdir + File.separator + out_classdir + outfilename);
        String table_name = "";
        primary_key_column_name = "";
        primary_key_column_autoincr_if_needed = "";
        primary_key_column_sqlitetype = "";
        BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(workdir + File.separator + infilename));
			String line = reader.readLine();

            boolean ignore_line = true;
			while (line != null) {
                // System.out.println("LLLLLLL: " + line.trim());
                if (line.trim().contains("@Table"))
                {
                    ignore_line = false;
                    line = reader.readLine();
                    while(line.trim().startsWith("@"))
                    {
                        line = reader.readLine();
                    }
                    table_name = line.trim().substring(line.trim().lastIndexOf(" ") + 1);
                    System.out.println("Table: " + table_name);
                    process_tablename(workdir, orma_global_out, table_name);
                }
                else if (ignore_line)
                {
                    line = reader.readLine();
                    continue;
                }
                else if (line.contains("______@@SORMA_END@@______"))
                {
                    break;
                }
                else if (line.trim().contains("@PrimaryKey"))
                {
                    line = reader.readLine();
                    while(line.trim().startsWith("@"))
                    {
                        line = reader.readLine();
                    }
                    System.out.println("PrimaryKey: " + line.trim());
                    primary_key_column_name = process_primary_key(workdir, infilename, outfilename, table_name,
                        line.trim());
                }
                else if (line.trim().contains("@Column"))
                {
                    line = reader.readLine();
                    while(line.trim().startsWith("@"))
                    {
                        line = reader.readLine();
                    }
                    // System.out.println("Column: " + line.trim());
                    process_column(workdir, infilename, outfilename,
                        line.trim(), table_name);
                }
				// System.out.println(line);
				line = reader.readLine();
			}

			reader.close();

            finish_table(workdir, table_name);

		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    static String remove_public(final String in)
    {
        if (in.trim().startsWith("public"))
        {
            return in.trim().substring("public".length()).trim();
        }
        else
        {
            return in;
        }
    }

    static String remove_type(final String in)
    {
        if (in.trim().toLowerCase().startsWith("int"))
        {
            return in.trim().substring("int".length()).trim();
        }
        else if (in.trim().toLowerCase().startsWith("long"))
        {
            return in.trim().substring("long".length()).trim();
        }
        else if (in.trim().toLowerCase().startsWith("string"))
        {
            return in.trim().substring("string".length()).trim();
        }
        else if (in.trim().toLowerCase().startsWith("boolean"))
        {
            return in.trim().substring("boolean".length()).trim();
        }
        else
        {
            return in;
        }
    }

    public static int min3(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    static String get_name(final String in)
    {
        String tmp = in.trim();
        int i1 = 999;
        int i2 = 999;
        int i3 = 999;
        try
        {
            i1 = tmp.indexOf(" ");
            if (i1<1){i1=999;}
        }
        catch(Exception e)
        {}
        try
        {
            i2 = tmp.indexOf("=");
            if (i2<1){i2=999;}
        }
        catch(Exception e)
        {}
        try
        {
            i3 = tmp.indexOf(";");
            if (i1<3){i3=999;}
        }
        catch(Exception e)
        {}

        // System.out.println(""+ i1 + " " + i2 + " " + i3 + " min=" + min3(i1, i2, i3));
        return tmp.substring(0, min3(i1, i2, i3)).trim();
    }

    static COLTYPE get_type(final String in)
    {
        if (in.trim().toLowerCase().startsWith("long"))
        {
            return COLTYPE.LONG;
        }
        else if (in.trim().toLowerCase().startsWith("int"))
        {
            return COLTYPE.INT;
        }
        else if (in.trim().toLowerCase().startsWith("string"))
        {
            return COLTYPE.STRING;
        }
        else if (in.trim().toLowerCase().startsWith("boolean"))
        {
            return COLTYPE.BOOLEAN;
        }
        return COLTYPE.UNKNOWN;
    }

    static String process_primary_key(final String workdir, final String infilename,
                                    final String outfilename, final String table_name,
                                    final String p)
    {
        final String p2 = remove_public(p);
        final String p3 = remove_type(p2);
        final String column_name = get_name(p3);
        final COLTYPE p5 = get_type(p2);
        final String javatype_firstupper = p5.javatype.substring(0,1).toUpperCase() + p5.javatype.substring(1);
        System.out.println("P: " + column_name + " type: " + p5.name);

        primary_key_column_autoincr_if_needed = "";
        primary_key_column_sqlitetype = p5.sqlitetype;
        if ((p5 == COLTYPE.INT) || (p5 == COLTYPE.LONG))
        {
            append_to_table(workdir, table_name, "    @PrimaryKey(autoincrement = true, auto = true)");
            primary_key_column_autoincr_if_needed = "AUTOINCREMENT";
        }
        else
        {
            append_to_table(workdir, table_name, "    @PrimaryKey");
            column_num++;
            // -----------
            String comma = "";
            if (column_num > 1) {comma = ",";}
            tbl_insert_sub01 += "                    + \""+comma+java_quoted+""+column_name+java_quoted+"\"" + "\n";
            tbl_insert_sub02 += "                    + \""+comma+"?"+column_num+"\"" + "\n";
            // -----------
            tbl_insert_sub03 += "            insert_pstmt.set"+javatype_firstupper+"("+column_num+", this."+column_name+");" + "\n";
        }
        append_to_table(workdir, table_name, "    public " + p5.javatype + " "+column_name+";");
        append_to_table(workdir, table_name, "");

        append_to_sql(workdir, table_name, "  \""+column_name+"\" "+primary_key_column_sqlitetype+",");

        tbl_deepcopy += "        out."+column_name+" = in."+column_name+";" + "\n";
        tbl_tostring += "\""+column_name+"=\" + "+column_name+"";
        tbl_tolist += "                out."+column_name+" = rs.get"+javatype_firstupper+"(\""+column_name+"\");" + "\n";

        add_equal_func(table_name, column_name, p5, javatype_firstupper);
        add_orderby_func(table_name, column_name, p5, javatype_firstupper);
        add_set_func(table_name, column_name, p5, javatype_firstupper);

        return(column_name);
    }

    static void process_column(final String workdir, final String infilename, final String outfilename, final String c, final String table_name)
    {
        final String c2 = remove_public(c);
        final String c3 = remove_type(c2);
        final String column_name = get_name(c3);
        final COLTYPE c5 = get_type(c2);
        System.out.println("C: " + column_name + " type: " + c5.name);

        append_to_table(workdir, table_name, "    @Column(indexed = true, helpers = Column.Helpers.ALL)");
        append_to_table(workdir, table_name, "    public " + c5.javatype + " " + column_name + ";");
        append_to_table(workdir, table_name, "");

        append_to_sql(workdir, table_name, "  \""+column_name+"\" "+c5.sqlitetype+",");

        column_num++;
        final String javatype_firstupper = c5.javatype.substring(0,1).toUpperCase() + c5.javatype.substring(1);
        tbl_deepcopy += "        out."+column_name+" = in."+column_name+";" + "\n";
        tbl_tostring += " + \", "+column_name+"=\" + "+column_name+"";
        tbl_tolist += "                out."+column_name+" = rs.get"+javatype_firstupper+"(\""+column_name+"\");" + "\n";
        // -----------
        String comma = "";
        if (column_num > 1) {comma = ",";}
        tbl_insert_sub01 += "                    + \""+comma+java_quoted+""+column_name+java_quoted+"\"" + "\n";
        tbl_insert_sub02 += "                    + \""+comma+"?"+column_num+"\"" + "\n";
        // -----------
        tbl_insert_sub03 += "            insert_pstmt.set"+javatype_firstupper+"("+column_num+", this."+column_name+");" + "\n";

        add_equal_func(table_name, column_name, c5, javatype_firstupper);
        add_orderby_func(table_name, column_name, c5, javatype_firstupper);
        add_set_func(table_name, column_name, c5, javatype_firstupper);
    }

    static void add_orderby_func(final String table_name, final String column_name, final COLTYPE ctype, final String javatype_firstupper)
    {
        final String column_name_firstupper = column_name.substring(0,1).toUpperCase() + column_name.substring(1);

        tbl_orderbyfuncs  += "    public "+table_name+" orderBy"+column_name_firstupper+"Asc()" + "\n";
        tbl_orderbyfuncs  += "    {" + "\n";
        tbl_orderbyfuncs  += "        if (this.sql_orderby.equals(\"\"))" + "\n";
        tbl_orderbyfuncs  += "        {" + "\n";
        tbl_orderbyfuncs  += "            this.sql_orderby = \" order by \";" + "\n";
        tbl_orderbyfuncs  += "        }" + "\n";
        tbl_orderbyfuncs  += "        else" + "\n";
        tbl_orderbyfuncs  += "        {" + "\n";
        tbl_orderbyfuncs  += "            this.sql_orderby = this.sql_orderby + \" , \";" + "\n";
        tbl_orderbyfuncs  += "        }" + "\n";
        tbl_orderbyfuncs  += "        this.sql_orderby = this.sql_orderby + \" \\\""+column_name+"\\\" ASC \";" + "\n";
        tbl_orderbyfuncs  += "        return this;" + "\n";
        tbl_orderbyfuncs  += "    }" + "\n";
        tbl_orderbyfuncs  += "" + "\n";

        tbl_orderbyfuncs  += "    public "+table_name+" orderBy"+column_name_firstupper+"Desc()" + "\n";
        tbl_orderbyfuncs  += "    {" + "\n";
        tbl_orderbyfuncs  += "        if (this.sql_orderby.equals(\"\"))" + "\n";
        tbl_orderbyfuncs  += "        {" + "\n";
        tbl_orderbyfuncs  += "            this.sql_orderby = \" order by \";" + "\n";
        tbl_orderbyfuncs  += "        }" + "\n";
        tbl_orderbyfuncs  += "        else" + "\n";
        tbl_orderbyfuncs  += "        {" + "\n";
        tbl_orderbyfuncs  += "            this.sql_orderby = this.sql_orderby + \" , \";" + "\n";
        tbl_orderbyfuncs  += "        }" + "\n";
        tbl_orderbyfuncs  += "        this.sql_orderby = this.sql_orderby + \" \\\""+column_name+"\\\" DESC \";" + "\n";
        tbl_orderbyfuncs  += "        return this;" + "\n";
        tbl_orderbyfuncs  += "    }" + "\n";
        tbl_orderbyfuncs  += "" + "\n";
    }

    static void add_set_func(final String table_name, final String column_name, final COLTYPE ctype, final String javatype_firstupper)
    {
        tbl_setfuncs  += "    public "+table_name+" "+column_name+"("+ctype.javatype+" "+column_name+")" + "\n";
        tbl_setfuncs  += "    {" + "\n";
        tbl_setfuncs  += "        if (this.sql_set.equals(\"\"))" + "\n";
        tbl_setfuncs  += "        {" + "\n";
        tbl_setfuncs  += "            this.sql_set = \" set \";" + "\n";
        tbl_setfuncs  += "        }" + "\n";
        tbl_setfuncs  += "        else" + "\n";
        tbl_setfuncs  += "        {" + "\n";
        tbl_setfuncs  += "            this.sql_set = this.sql_set + \" , \";" + "\n";
        tbl_setfuncs  += "        }" + "\n";
        tbl_setfuncs  += "        this.sql_set = this.sql_set + \" \\\""+column_name+"\\\"=?\" + (BINDVAR_OFFSET_SET + bind_set_count) + \" \";" + "\n";
        tbl_setfuncs  += "        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"));" + "\n";
        tbl_setfuncs  += "        bind_set_count++;" + "\n";
        tbl_setfuncs  += "        return this;" + "\n";
        tbl_setfuncs  += "    }" + "\n";
        tbl_setfuncs  += "" + "\n";
    }

    static void add_equal_func(final String table_name, final String column_name, final COLTYPE ctype, final String javatype_firstupper)
    {
        // Eq
        tbl_equalfuncs  += "    public "+table_name+" "+column_name+"Eq("+ctype.javatype+" "+column_name+")" + "\n";
        tbl_equalfuncs  += "    {" + "\n";
        tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\"=?\" + (BINDVAR_OFFSET_WHERE + bind_where_count) + \" \";" + "\n";
        tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"));" + "\n";
        tbl_equalfuncs  += "        bind_where_count++;" + "\n";
        tbl_equalfuncs  += "        return this;" + "\n";
        tbl_equalfuncs  += "    }" + "\n";
        tbl_equalfuncs  += "" + "\n";

        // NotEq
        tbl_equalfuncs  += "    public "+table_name+" "+column_name+"NotEq("+ctype.javatype+" "+column_name+")" + "\n";
        tbl_equalfuncs  += "    {" + "\n";
        tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\"<>?\" + (BINDVAR_OFFSET_WHERE + bind_where_count) + \" \";" + "\n";
        tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"));" + "\n";
        tbl_equalfuncs  += "        bind_where_count++;" + "\n";
        tbl_equalfuncs  += "        return this;" + "\n";
        tbl_equalfuncs  += "    }" + "\n";
        tbl_equalfuncs  += "" + "\n";

        if ((ctype == COLTYPE.INT)||(ctype == COLTYPE.LONG))
        {
            // Lt
            tbl_equalfuncs  += "    public "+table_name+" "+column_name+"Lt("+ctype.javatype+" "+column_name+")" + "\n";
            tbl_equalfuncs  += "    {" + "\n";
            tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\"<?\" + (BINDVAR_OFFSET_WHERE + bind_where_count) + \" \";" + "\n";
            tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"));" + "\n";
            tbl_equalfuncs  += "        bind_where_count++;" + "\n";
            tbl_equalfuncs  += "        return this;" + "\n";
            tbl_equalfuncs  += "    }" + "\n";
            tbl_equalfuncs  += "" + "\n";

            // Le
            tbl_equalfuncs  += "    public "+table_name+" "+column_name+"Le("+ctype.javatype+" "+column_name+")" + "\n";
            tbl_equalfuncs  += "    {" + "\n";
            tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\"<=?\" + (BINDVAR_OFFSET_WHERE + bind_where_count) + \" \";" + "\n";
            tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"));" + "\n";
            tbl_equalfuncs  += "        bind_where_count++;" + "\n";
            tbl_equalfuncs  += "        return this;" + "\n";
            tbl_equalfuncs  += "    }" + "\n";
            tbl_equalfuncs  += "" + "\n";

            // Gt
            tbl_equalfuncs  += "    public "+table_name+" "+column_name+"Gt("+ctype.javatype+" "+column_name+")" + "\n";
            tbl_equalfuncs  += "    {" + "\n";
            tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\">?\" + (BINDVAR_OFFSET_WHERE + bind_where_count) + \" \";" + "\n";
            tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"));" + "\n";
            tbl_equalfuncs  += "        bind_where_count++;" + "\n";
            tbl_equalfuncs  += "        return this;" + "\n";
            tbl_equalfuncs  += "    }" + "\n";
            tbl_equalfuncs  += "" + "\n";

            // Ge
            tbl_equalfuncs  += "    public "+table_name+" "+column_name+"Ge("+ctype.javatype+" "+column_name+")" + "\n";
            tbl_equalfuncs  += "    {" + "\n";
            tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\">=?\" + (BINDVAR_OFFSET_WHERE + bind_where_count) + \" \";" + "\n";
            tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"));" + "\n";
            tbl_equalfuncs  += "        bind_where_count++;" + "\n";
            tbl_equalfuncs  += "        return this;" + "\n";
            tbl_equalfuncs  += "    }" + "\n";
            tbl_equalfuncs  += "" + "\n";

            // Between
            tbl_equalfuncs  += "    public "+table_name+" "+column_name+"Between("+ctype.javatype+" "+column_name+"1, "+ctype.javatype+" "+column_name+"2)" + "\n";
            tbl_equalfuncs  += "    {" + "\n";
            tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\">?\" + (BINDVAR_OFFSET_WHERE + bind_where_count) + \" and "+column_name+"<?\" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + \" \";" + "\n";
            tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"1));" + "\n";
            tbl_equalfuncs  += "        bind_where_count++;" + "\n";
            tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"2));" + "\n";
            tbl_equalfuncs  += "        bind_where_count++;" + "\n";
            tbl_equalfuncs  += "        return this;" + "\n";
            tbl_equalfuncs  += "    }" + "\n";
            tbl_equalfuncs  += "" + "\n";
        }

        // Null
        tbl_equalfuncs  += "    public "+table_name+" "+column_name+"IsNull()" + "\n";
        tbl_equalfuncs  += "    {" + "\n";
        tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\" IS NULL \";" + "\n";
        tbl_equalfuncs  += "        return this;" + "\n";
        tbl_equalfuncs  += "    }" + "\n";
        tbl_equalfuncs  += "" + "\n";

        // NotNull
        tbl_equalfuncs  += "    public "+table_name+" "+column_name+"IsNotNull()" + "\n";
        tbl_equalfuncs  += "    {" + "\n";
        tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\" IS NOT NULL \";" + "\n";
        tbl_equalfuncs  += "        return this;" + "\n";
        tbl_equalfuncs  += "    }" + "\n";
        tbl_equalfuncs  += "" + "\n";

        if (ctype == COLTYPE.STRING)
        {
            // Like
            tbl_equalfuncs  += "    public "+table_name+" "+column_name+"Like("+ctype.javatype+" "+column_name+")" + "\n";
            tbl_equalfuncs  += "    {" + "\n";
            tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\" LIKE ?\" + (BINDVAR_OFFSET_WHERE + bind_where_count) + \" ESCAPE '\\\\' \";" + "\n";
            tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"));" + "\n";
            tbl_equalfuncs  += "        bind_where_count++;" + "\n";
            tbl_equalfuncs  += "        return this;" + "\n";
            tbl_equalfuncs  += "    }" + "\n";
            tbl_equalfuncs  += "" + "\n";

            // NotLike
            tbl_equalfuncs  += "    public "+table_name+" "+column_name+"NotLike("+ctype.javatype+" "+column_name+")" + "\n";
            tbl_equalfuncs  += "    {" + "\n";
            tbl_equalfuncs  += "        this.sql_where = this.sql_where + \" and \\\""+column_name+"\\\" NOT LIKE ?\" + (BINDVAR_OFFSET_WHERE + bind_where_count) + \" ESCAPE '\\\\' \";" + "\n";
            tbl_equalfuncs  += "        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_"+javatype_firstupper+", "+column_name+"));" + "\n";
            tbl_equalfuncs  += "        bind_where_count++;" + "\n";
            tbl_equalfuncs  += "        return this;" + "\n";
            tbl_equalfuncs  += "    }" + "\n";
            tbl_equalfuncs  += "" + "\n";
        }
    }
}

package workspace.commons.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import workspace.commons.model.XmlCallMapping;
import workspace.commons.model.XmlMyBatisMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MappersServiceTest {

    @Nested
    class ExtractFunctionCallTest {
        @Test
        public void testExtractFunctionCallOracle() {
            String content =
                "{\n" +
                "  call pseg_Usuario.Sp_Consultar_Registro_Login(\n" +
                "    #{userLogin, jdbcType=VARCHAR, mode=IN},\n" +
                "    #{resultSet, jdbcType=CURSOR,  mode=OUT, javaType=java.sql.ResultSet, resultMap=loadUserByUsernameResult}\n" +
                "  )\n" +
                "}";
            String result = MappersService.extractFunctionCall(content);
            assertEquals("pseg_Usuario.Sp_Consultar_Registro_Login()".toUpperCase(), result);

            content =
                "call pseg_Usuario.Sp_Consultar_Registro_Login(\n" +
                "  #{userLogin, jdbcType=VARCHAR, mode=IN},\n" +
                "  #{resultSet, jdbcType=CURSOR,  mode=OUT, javaType=java.sql.ResultSet, resultMap=loadUserByUsernameResult}\n" +
                ")";
            result = MappersService.extractFunctionCall(content);
            assertEquals("pseg_Usuario.Sp_Consultar_Registro_Login()".toUpperCase(), result);

            content =
                "{\n" +
                "  call Srq_Usuario.Sp_Validar(\n" +
                "    #{userLogin,    jdbcType=VARCHAR, mode=IN},\n" +
                "    #{userIp,       jdbcType=VARCHAR, mode=IN},\n" +
                "    #{password,     jdbcType=VARCHAR, mode=IN},\n" +
                "    #{appName,      jdbcType=VARCHAR, mode=IN},\n" +
                "    #{validCaptcha, jdbcType=VARCHAR, mode=IN},\n" +
                "    #{resultSet,    jdbcType=CURSOR,  mode=OUT, javaType=java.sql.ResultSet, resultMap=Authentication_singleUserResult}\n" +
                "  )\n" +
                "}";
            result = MappersService.extractFunctionCall(content);
            assertEquals("Srq_Usuario.Sp_Validar()".toUpperCase(), result);

            content =
                "{\n" +
                "  #{id, jdbcType=INTEGER, mode=OUT} = call fn_ng_user2factoraut_ins(\n" +
                "    #{userId,                  jdbcType=INTEGER, mode=IN},\n" +
                "    #{lastPassword,            jdbcType=VARCHAR, mode=IN},\n" +
                "    #{timeMillisFromLastLogin, jdbcType=INTEGER, mode=IN},\n" +
                "    #{totpSecret,              jdbcType=VARCHAR, mode=IN}\n" +
                "  )\n" +
                "}";
            result = MappersService.extractFunctionCall(content);
            assertEquals("fn_ng_user2factoraut_ins()".toUpperCase(), result);
        }

        @Test
        public void testExtractFunctionCallPostgres() {
            String content =
                "{\n" +
                "  call sec.f_user_by_login_get(\n" +
                "    #{ userLogin, jdbcType=VARCHAR, mode=IN },\n" +
                "    #{ resultSet, jdbcType=OTHER,   mode=OUT, javaType=java.sql.ResultSet, resultMap=loadUserByUsernameResult }\n" +
                "  )\n" +
                "}";
            String result = MappersService.extractFunctionCall(content);
            assertEquals("sec.f_user_by_login_get()".toUpperCase(), result);

            content =
                "call sec.sp_user_validate(\n" +
                "  #{ userLogin,    jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ userIp,       jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ password,     jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ appName,      jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ validCaptcha, jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ message,      jdbcType=VARCHAR, mode=OUT },\n" +
                "  #{ resultSet,    jdbcType=OTHER,   mode=OUT, javaType=java.sql.ResultSet, resultMap=authenticationSingleUserResult }\n" +
                ")";
            result = MappersService.extractFunctionCall(content);
            assertEquals("sec.sp_user_validate()".toUpperCase(), result);

            content =
                "{\n" +
                "  #{resultSet, jdbcType=OTHER, mode=OUT,javaType=java.sql.ResultSet,resultMap=listInterceptionLogMap} = call log.f_log_interception_list(\n" +
                "    #{userLogin, jdbcType=VARCHAR, mode=IN},\n" +
                "    #{userIp,    jdbcType=VARCHAR, mode=IN},\n" +
                "    #{filters,   jdbcType=VARCHAR, mode=IN},\n" +
                "    #{fields,    jdbcType=VARCHAR, mode=IN}\n" +
                "  )\n" +
                "}";
            result = MappersService.extractFunctionCall(content);
            assertEquals("log.f_log_interception_list()".toUpperCase(), result);
        }

        @Test
        public void testExtractFunctionCallEdgeCases() {
            // Test empty string
            String result = MappersService.extractFunctionCall("");
            assertEquals("", result);

            // Test null content handling (if applicable)
            result = MappersService.extractFunctionCall("   ");
            assertEquals("", result);

            // Test string without "call" keyword
            result = MappersService.extractFunctionCall("select * from table");
            assertEquals("", result);

            // Test multiple spaces after "call"
            String content = "call   schema.function()";
            result = MappersService.extractFunctionCall(content);
            assertEquals("SCHEMA.FUNCTION()".toUpperCase(), result);

            // Test tab character after "call"
            content = "call\tdb.func()";
            result = MappersService.extractFunctionCall(content);
            assertEquals("DB.FUNC()".toUpperCase(), result);

            // Test function name with numbers and underscores
            content = "call pkg_func123.sp_my_function_2()";
            result = MappersService.extractFunctionCall(content);
            assertEquals("PKG_FUNC123.SP_MY_FUNCTION_2()".toUpperCase(), result);
        }

        @Test
        public void testExtractFunctionCallCaseSensitivity() {
            // Test lowercase "call"
            String content = "call myschema.myfunc()";
            String result = MappersService.extractFunctionCall(content);
            assertEquals("MYSCHEMA.MYFUNC()", result);

            // Test uppercase "CALL"
            content = "CALL schema.function()";
            result = MappersService.extractFunctionCall(content);
            assertEquals("SCHEMA.FUNCTION()", result);

            // Test mixed case input
            content = "call MySchema.MyFunction()";
            result = MappersService.extractFunctionCall(content);
            assertEquals("MYSCHEMA.MYFUNCTION()", result);
        }

        @Test
        public void testExtractFunctionCallWithLeadingWhitespace() {
            // Test with leading spaces
            String content = "   call schema.function()";
            String result = MappersService.extractFunctionCall(content);
            assertEquals("SCHEMA.FUNCTION()".toUpperCase(), result);

            // Test with leading newlines
            content = "\n\ncall db.proc()";
            result = MappersService.extractFunctionCall(content);
            assertEquals("DB.PROC()".toUpperCase(), result);

            // Test with leading tabs
            content = "\t\tcall owner.pkg_func()";
            result = MappersService.extractFunctionCall(content);
            assertEquals("OWNER.PKG_FUNC()".toUpperCase(), result);
        }

        @Test
        public void testExtractFunctionCallComplexContent() {
            // Test function call in middle of content
            String content = "#{result} = call app.fn_process() and more text";
            String result = MappersService.extractFunctionCall(content);
            assertEquals("APP.FN_PROCESS()".toUpperCase(), result);

            // Test function call with multiline content after
            content = "call util.execute_task()\nand other stuff";
            result = MappersService.extractFunctionCall(content);
            assertEquals("UTIL.EXECUTE_TASK()".toUpperCase(), result);
        }
    }

    @Nested
    class ExtractFunctionParamsTest {
        @Test
        public void testExtractFunctionParamsOracle() {
            String content =
                "{\n" +
                "  call pseg_Usuario.Sp_Consultar_Registro_Login(\n" +
                "    #{userLogin, jdbcType=VARCHAR, mode=IN},\n" +
                "    #{resultSet, jdbcType=CURSOR,  mode=OUT, javaType=java.sql.ResultSet, resultMap=loadUserByUsernameResult}\n" +
                "  )\n" +
                "}";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(1, result.size());
            assertEquals("userLogin", result.get(0));
        }

        @Test
        public void testExtractFunctionParamsMultipleInputParams() {
            String content =
                "{\n" +
                "  call Srq_Usuario.Sp_Validar(\n" +
                "    #{userLogin,    jdbcType=VARCHAR, mode=IN},\n" +
                "    #{userIp,       jdbcType=VARCHAR, mode=IN},\n" +
                "    #{password,     jdbcType=VARCHAR, mode=IN},\n" +
                "    #{appName,      jdbcType=VARCHAR, mode=IN},\n" +
                "    #{validCaptcha, jdbcType=VARCHAR, mode=IN},\n" +
                "    #{resultSet,    jdbcType=CURSOR,  mode=OUT, javaType=java.sql.ResultSet, resultMap=Authentication_singleUserResult}\n" +
                "  )\n" +
                "}";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(5, result.size());
            assertEquals("userLogin", result.get(0));
            assertEquals("userIp", result.get(1));
            assertEquals("password", result.get(2));
            assertEquals("appName", result.get(3));
            assertEquals("validCaptcha", result.get(4));
        }

        @Test
        public void testExtractFunctionParamsPostgres() {
            String content =
                "{\n" +
                "  call sec.f_user_by_login_get(\n" +
                "    #{ userLogin, jdbcType=VARCHAR, mode=IN },\n" +
                "    #{ resultSet, jdbcType=OTHER,   mode=OUT, javaType=java.sql.ResultSet, resultMap=loadUserByUsernameResult }\n" +
                "  )\n" +
                "}";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(1, result.size());
            assertEquals("userLogin", result.get(0));
        }

        @Test
        public void testExtractFunctionParamsPostgresMultiple() {
            String content =
                "call sec.sp_user_validate(\n" +
                "  #{ userLogin,    jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ userIp,       jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ password,     jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ appName,      jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ validCaptcha, jdbcType=VARCHAR, mode=IN  },\n" +
                "  #{ message,      jdbcType=VARCHAR, mode=OUT },\n" +
                "  #{ resultSet,    jdbcType=OTHER,   mode=OUT, javaType=java.sql.ResultSet, resultMap=authenticationSingleUserResult }\n" +
                ")";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(5, result.size());
            assertEquals("userLogin", result.get(0));
            assertEquals("userIp", result.get(1));
            assertEquals("password", result.get(2));
            assertEquals("appName", result.get(3));
            assertEquals("validCaptcha", result.get(4));
        }

        @Test
        public void testExtractFunctionParamsIgnoresOutMode() {
            String content =
                "{\n" +
                "  #{id, jdbcType=INTEGER, mode=OUT} = call fn_ng_user2factoraut_ins(\n" +
                "    #{userId,                  jdbcType=INTEGER, mode=IN},\n" +
                "    #{lastPassword,            jdbcType=VARCHAR, mode=IN},\n" +
                "    #{timeMillisFromLastLogin, jdbcType=INTEGER, mode=IN},\n" +
                "    #{totpSecret,              jdbcType=VARCHAR, mode=IN}\n" +
                "  )\n" +
                "}";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(4, result.size());
            assertEquals("userId", result.get(0));
            assertEquals("lastPassword", result.get(1));
            assertEquals("timeMillisFromLastLogin", result.get(2));
            assertEquals("totpSecret", result.get(3));
        }

        @Test
        public void testExtractFunctionParamsEmptyString() {
            String content = "";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(0, result.size());
        }

        @Test
        public void testExtractFunctionParamsNoParams() {
            String content = "{\n  call schema.function()\n}";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(0, result.size());
        }

        @Test
        public void testExtractFunctionParamsOnlyOutMode() {
            String content =
                "#{ message,      jdbcType=VARCHAR, mode=OUT },\n" +
                "#{ resultSet,    jdbcType=OTHER,   mode=OUT, javaType=java.sql.ResultSet }";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(0, result.size());
        }

        @Test
        public void testExtractFunctionParamsWithVariousSpacing() {
            String content =
                "#{userLogin, jdbcType=VARCHAR, mode=IN},\n" +
                "#{ userIp, jdbcType=VARCHAR, mode=IN },\n" +
                "#{  password,    jdbcType=VARCHAR,    mode=IN  }";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(3, result.size());
            assertEquals("userLogin", result.get(0));
            assertEquals("userIp", result.get(1));
            assertEquals("password", result.get(2));
        }

        @Test
        public void testExtractFunctionParamsWithUnderscoresAndNumbers() {
            String content =
                "#{user_login_1, jdbcType=VARCHAR, mode=IN},\n" +
                "#{param_name_2, jdbcType=INTEGER, mode=IN},\n" +
                "#{id_3, jdbcType=BIGINT, mode=IN}";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(3, result.size());
            assertEquals("user_login_1", result.get(0));
            assertEquals("param_name_2", result.get(1));
            assertEquals("id_3", result.get(2));
        }

        @Test
        public void testExtractFunctionParamsMixedWithoutAndInMode() {
            String content =
                "#{id, mode=OUT},\n" +
                "#{firstName, mode=IN},\n" +
                "#{lastName, mode=IN},\n" +
                "#{message, mode=OUT}";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(2, result.size());
            assertEquals("firstName", result.get(0));
            assertEquals("lastName", result.get(1));
        }

        @Test
        public void testExtractFunctionParamsOrderPreservation() {
            String content =
                "#{param1, mode=IN},\n" +
                "#{param2, mode=IN},\n" +
                "#{param3, mode=IN},\n" +
                "#{param4, mode=IN},\n" +
                "#{param5, mode=IN}";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(5, result.size());
            assertEquals("param1", result.get(0));
            assertEquals("param2", result.get(1));
            assertEquals("param3", result.get(2));
            assertEquals("param4", result.get(3));
            assertEquals("param5", result.get(4));
        }

        @Test
        public void testExtractFunctionParamsComplexContent() {
            String content =
                "{\n" +
                "  #{resultSet, jdbcType=OTHER, mode=OUT,javaType=java.sql.ResultSet,resultMap=listInterceptionLogMap} = call log.f_log_interception_list(\n" +
                "    #{userLogin, jdbcType=VARCHAR, mode=IN},\n" +
                "    #{userIp,    jdbcType=VARCHAR, mode=IN},\n" +
                "    #{filters,   jdbcType=VARCHAR, mode=IN},\n" +
                "    #{fields,    jdbcType=VARCHAR, mode=IN}\n" +
                "  )\n" +
                "}";
            var result = MappersService.extractFunctionParams(content);
            assertEquals(4, result.size());
            assertEquals("userLogin", result.get(0));
            assertEquals("userIp", result.get(1));
            assertEquals("filters", result.get(2));
            assertEquals("fields", result.get(3));
        }
    }

    @Nested
    class GetXmlMappingsTest {
        @Test
        public void testGetXmlMappings_NoSelect() {
            String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"com.example.EmptyMapper\">\n" +
                "</mapper>";

            try {
                String database = "oracle";
                XmlMyBatisMapping result = MappersService.getXmlMappings(content, database);
                assertEquals("com.example.EmptyMapper", result.getNamespace());
                assertEquals(database, result.getDatabase());
                assertEquals(0, result.getSelects().size());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        public void testGetXmlMappings_OneSelect() {
            String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"com.suntech.vigiaNG.userservice.repository.UserMapper\">\n" +
                "    <select id=\"listUser\" resultMap=\"listUserResult\" parameterType=\"java.util.Map\" statementType=\"CALLABLE\" fetchSize=\"1000\">\n" +
                "        {\n" +
                "            #{resultSet, jdbcType=CURSOR, mode=OUT,javaType=java.sql.ResultSet,resultMap=listUserResult} = call fn_ng_users_list(\n" +
                "                #{userLogin,  jdbcType=VARCHAR,  mode=IN},\n" +
                "                #{userIp,     jdbcType=VARCHAR,  mode=IN},\n" +
                "                #{filters,     jdbcType=VARCHAR,  mode=IN},\n" +
                "                #{fields,     jdbcType=VARCHAR,  mode=IN}\n" +
                "            )\n" +
                "        }\n" +
                "    </select>\n" +
                "</mapper>";

            try {
                String database = "oracle";
                XmlMyBatisMapping result = MappersService.getXmlMappings(content, database);
                assertEquals("com.suntech.vigiaNG.userservice.repository.UserMapper", result.getNamespace());
                assertEquals(database, result.getDatabase());
                assertEquals(1, result.getSelects().size());

                XmlCallMapping select1 = result.getSelects().get(0);
                assertEquals("listUser", select1.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        public void testGetXmlMappings_TwoSelects() {
            String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"com.example.MyMapper\">\n" +
                "    <select id=\"getUser\" resultMap=\"getUserResult\" parameterType=\"java.util.Map\" statementType=\"CALLABLE\">\n" +
                "        {\n" +
                "            #{resultSet, jdbcType=CURSOR, mode=OUT,javaType=java.sql.ResultSet,resultMap=getUserResult} = call fn_get_user(\n" +
                "                #{userId,  jdbcType=INTEGER,  mode=IN}\n" +
                "            )\n" +
                "        }\n" +
                "    </select>\n" +
                "    <select id=\"listUsers\" resultMap=\"listUsersResult\" parameterType=\"java.util.Map\" statementType=\"CALLABLE\">\n" +
                "        {\n" +
                "            #{resultSet, jdbcType=CURSOR, mode=OUT,javaType=java.sql.ResultSet,resultMap=listUsersResult} = call fn_list_users()\n" +
                "        }\n" +
                "    </select>\n" +
                "</mapper>";

            try {
                String database = "oracle";
                XmlMyBatisMapping result = MappersService.getXmlMappings(content, database);
                assertEquals("com.example.MyMapper", result.getNamespace());
                assertEquals(database, result.getDatabase());
                assertEquals(2, result.getSelects().size());

                XmlCallMapping select1 = result.getSelects().get(0);
                assertEquals("getUser", select1.getId());

                XmlCallMapping select2 = result.getSelects().get(1);
                assertEquals("listUsers", select2.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        public void testGetXmlMappings_NoInsert() {
            String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"com.example.EmptyMapper\">\n" +
                "</mapper>";

            try {
                String database = "oracle";
                XmlMyBatisMapping result = MappersService.getXmlMappings(content, database);
                assertEquals("com.example.EmptyMapper", result.getNamespace());
                assertEquals(database, result.getDatabase());
                assertEquals(0, result.getInserts().size());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        public void testGetXmlMappings_OneInsert() {
            String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"com.example.MyMapper\">\n" +
                "    <insert id=\"saveNoteUsers\" parameterType=\"java.util.Map\" statementType=\"CALLABLE\">\n" +
                "       {\n" +
                "           call conf.f_notes_user_ins(\n" +
                "               #{userLogin, jdbcType=VARCHAR,   mode=IN},\n" +
                "               #{userIp,    jdbcType=VARCHAR,   mode=IN},\n" +
                "               #{noteId,    jdbcType=BIGINT,    mode=IN}::integer,\n" +
                "               #{login,     jdbcType=VARCHAR,   mode=IN},\n" +
                "               #{date,      jdbcType=TIMESTAMP, mode=IN},\n" +
                "               #{closed,    jdbcType=VARCHAR,   mode=IN},\n" +
                "               #{read,      jdbcType=VARCHAR,   mode=IN}\n" +
                "           )\n" +
                "       }\n" +
                "   </insert>\n" +
                "</mapper>";

            try {
                String database = "oracle";
                XmlMyBatisMapping result = MappersService.getXmlMappings(content, database);
                assertEquals("com.example.MyMapper", result.getNamespace());
                assertEquals(database, result.getDatabase());
                assertEquals(1, result.getInserts().size());

                XmlCallMapping insert1 = result.getInserts().get(0);
                assertEquals("saveNoteUsers", insert1.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        public void testGetXmlMappings_TwoInserts() {
            String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"com.example.MyMapper\">\n" +
                "    <insert id=\"insertOne\" parameterType=\"java.util.Map\" statementType=\"CALLABLE\">\n" +
                "       {\n" +
                "           call schema.proc_insert_one( #{param1, jdbcType=VARCHAR, mode=IN} )\n" +
                "       }\n" +
                "   </insert>\n" +
                "    <insert id=\"insertTwo\" parameterType=\"java.util.Map\" statementType=\"CALLABLE\">\n" +
                "       {\n" +
                "           call schema.proc_insert_two( #{param2, jdbcType=INTEGER, mode=IN} )\n" +
                "       }\n" +
                "   </insert>\n" +
                "</mapper>";

            try {
                String database = "oracle";
                XmlMyBatisMapping result = MappersService.getXmlMappings(content, database);
                assertEquals("com.example.MyMapper", result.getNamespace());
                assertEquals(database, result.getDatabase());
                assertEquals(2, result.getInserts().size());

                XmlCallMapping insert1 = result.getInserts().get(0);
                assertEquals("insertOne", insert1.getId());

                XmlCallMapping insert2 = result.getInserts().get(1);
                assertEquals("insertTwo", insert2.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}

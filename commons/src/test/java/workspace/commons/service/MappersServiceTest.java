package workspace.commons.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MappersServiceTest {

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

    @Nested
    class ExtractFunctionCallTest {
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
        public void testExtractFunctionCallInComplexContent() {
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

}

package workspace.vigiang.service;

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

}

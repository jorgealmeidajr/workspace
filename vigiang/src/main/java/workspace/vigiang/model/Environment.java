package workspace.vigiang.model;

public enum Environment {

    ALGAR(Database.ORACLE),
    CLARO(Database.ORACLE),
    LIGGA(Database.ORACLE),
    OI(Database.ORACLE),
    SKY(Database.ORACLE),
    SURF(Database.POSTGRES),
    TIM(Database.ORACLE),
    VIVO(Database.ORACLE),
    VTAL(Database.ORACLE),
    WOM(Database.ORACLE),
    WOM2(Database.POSTGRES);

    private final Database database;

    Environment(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public VigiaNgDAO getVigiaNgDAO() {
        if (Database.ORACLE.equals(database)) return new OracleVigiaNgDAO();
        if (Database.POSTGRES.equals(database)) return new PostgresVigiaNgDAO();
        return null;
    }

    public enum Database {
        ORACLE, POSTGRES
    }

}
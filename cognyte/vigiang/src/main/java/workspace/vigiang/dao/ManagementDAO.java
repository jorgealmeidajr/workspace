package workspace.vigiang.dao;

import workspace.vigiang.model.ManagementConfiguration;
import workspace.vigiang.model.ManagementFeature;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagementDAO {

    public Map<String, Integer> listCarriers() throws SQLException {
        Map<String, Integer> data = new HashMap<>();

        String sql =
            "select id, upper(name) as \"name\" from carrier\n" +
            "order by name";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String name = rs.getString("name").trim();
                data.put(name, rs.getInt("id"));
            }
        }
        return data;
    }

    public List<ManagementConfiguration> listConfigurations() throws SQLException {
        List<ManagementConfiguration> dataset = new ArrayList<>();

        String sql =
            "select * from \"configuration\" c \n" +
            "order by id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                ManagementConfiguration row = new ManagementConfiguration();

                row.setId(rs.getInt("id"));
                row.setDescriptionPt(rs.getString("descriptionPt"));
                row.setDescriptionEn(rs.getString("descriptionEn"));
                row.setDescriptionEs(rs.getString("descriptionEs"));
                row.setName(rs.getString("name").trim());
                row.setPattern(rs.getString("pattern"));

                dataset.add(row);
            }
        }
        return dataset;
    }

    public List<ManagementFeature> listFeatures() throws SQLException {
        List<ManagementFeature> dataset = new ArrayList<>();

        String sql =
            "select * from feature f\n" +
            "order by id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                ManagementFeature row = new ManagementFeature();

                row.setId(rs.getInt("id"));
                row.setDescription(rs.getString("description"));
                row.setName(rs.getString("name").trim());

                dataset.add(row);
            }
        }
        return dataset;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:postgresql://10.50.150.182:5432/postgres",
                "postgres",
                "postgres");
    }

}

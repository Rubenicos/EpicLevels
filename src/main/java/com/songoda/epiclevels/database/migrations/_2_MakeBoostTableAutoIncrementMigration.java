package com.songoda.epiclevels.database.migrations;

import com.songoda.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _2_MakeBoostTableAutoIncrementMigration extends DataMigration {
    public _2_MakeBoostTableAutoIncrementMigration() {
        super(2);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "boosts MODIFY id INTEGER AUTO_INCREMENT");
        }
    }
}

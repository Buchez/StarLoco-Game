package org.starloco.locos.database.data.game;

import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.database.data.FunctionDAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestion des cartes de monstres équipées par les joueurs.
 *
 * Maximum : 3 cartes par joueur.
 *
 * slot -> monster_id
 */
public class PlayerMonsterCardData extends FunctionDAO<Object> {

    public PlayerMonsterCardData(HikariDataSource dataSource) {
        super(dataSource, "player_monster_cards");
    }

    /**
     * Retourne les cartes équipées d'un joueur.
     *
     * La LinkedHashMap conserve l'ordre des slots.
     */
    public Map<Integer, Integer> getEquippedMonsterIds(int playerId) {
        Map<Integer, Integer> result = new LinkedHashMap<>();

        try {
            String query =
                    "SELECT slot, monster_id " +
                    "FROM " + getTableName() +
                    " WHERE player_id = ? " +
                    "ORDER BY slot ASC";

            PreparedStatement statement = getPreparedStatement(query);

            if (statement == null)
                return result;

            statement.setInt(1, playerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.put(
                            rs.getInt("slot"),
                            rs.getInt("monster_id")
                    );
                }
            }

            close(statement);

        } catch (SQLException e) {
            sendError(e);
        }

        return result;
    }

    /**
     * Équipe une carte dans un slot.
     *
     * Vérifie :
     * - slot 1 à 7
     * - carte existante
     * - correspondance carte -> monstre
     * - pas de doublon de monstre
     */
    public boolean equip(int playerId, int slot, int cardItemId) {

        if (slot < 1 || slot > 7)
            return false;

        MonsterCardData monsterCardData =
                org.starloco.locos.database.DatabaseManager.get(MonsterCardData.class);

        if (monsterCardData == null)
            return false;

        int monsterId =
                monsterCardData.getMonsterIdByCardItemId(cardItemId);

        if (monsterId <= 0)
            return false;

        Map<Integer, Integer> equipped =
                getEquippedMonsterIds(playerId);

        // Interdit deux fois le même monstre.
        for (Map.Entry<Integer, Integer> entry : equipped.entrySet()) {
            if (entry.getKey() != slot
                    && entry.getValue() == monsterId) {
                return false;
            }
        }

        try {
            String query =
                    "INSERT INTO " + getTableName() +
                    " (player_id, slot, card_item_id, monster_id) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "card_item_id = VALUES(card_item_id), " +
                    "monster_id = VALUES(monster_id)";

            PreparedStatement statement = getPreparedStatement(query);

            if (statement == null)
                return false;

            statement.setInt(1, playerId);
            statement.setInt(2, slot);
            statement.setInt(3, cardItemId);
            statement.setInt(4, monsterId);

            statement.executeUpdate();

            close(statement);

            return true;

        } catch (SQLException e) {
            sendError(e);
            return false;
        }
    }

    /**
     * Retire une carte d'un slot.
     */
    public boolean unequip(int playerId, int slot) {

        if (slot < 1 || slot > 7)
            return false;

        try {
            String query =
                    "DELETE FROM " + getTableName() +
                    " WHERE player_id = ? AND slot = ?";

            PreparedStatement statement = getPreparedStatement(query);

            if (statement == null)
                return false;

            statement.setInt(1, playerId);
            statement.setInt(2, slot);

            statement.executeUpdate();

            close(statement);

            return true;

        } catch (SQLException e) {
            sendError(e);
            return false;
        }
    }

    @Override
    public void loadFully() {
        // Les cartes équipées sont lues à la demande.
    }

    @Override
    public Object load(int id) {
        return null;
    }

    @Override
    public boolean insert(Object entity) {
        return false;
    }

    @Override
    public void delete(Object entity) {
    }

    @Override
    public void update(Object entity) {
    }

    @Override
    public Class<?> getReferencedClass() {
        return PlayerMonsterCardData.class;
    }
}
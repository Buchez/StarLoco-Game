package org.starloco.locos.database.data.game;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang.NotImplementedException;
import org.starloco.locos.database.data.FunctionDAO;
import org.starloco.locos.game.world.World;
import org.starloco.locos.kernel.Main;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestion des cartes de monstres.
 *
 * Cette classe charge la correspondance :
 *
 *     monster_id -> card_item_id
 *
 * Exemple :
 *
 *     972 -> 16267
 *
 * signifie que le Jeune Boufton Blanc (monster_id 972)
 * possède la carte item 16267.
 *
 * La table SQL est volontairement générique afin de pouvoir
 * gérer tous les monstres du jeu sans modifier le code Java
 * pour chaque nouveau monstre.
 */
public class MonsterCardData extends FunctionDAO<Object> {

    /**
     * Correspondance en mémoire :
     *
     * monsterId -> cardItemId
     */
    private final Map<Integer, Integer> cards = new HashMap<>();

    public MonsterCardData(HikariDataSource dataSource) {
        super(dataSource, "monster_cards");
    }

    /**
     * Charge toutes les cartes depuis MariaDB au démarrage du serveur.
     */
    @Override
    public void loadFully() {
        try {
            getData(
                    "SELECT monster_id, card_item_id " +
                    "FROM " + getTableName() + " " +
                    "WHERE enabled = 1;",
                    result -> {

                        cards.clear();

                        while (result.next()) {
                            int monsterId = result.getInt("monster_id");
                            int cardItemId = result.getInt("card_item_id");

                            cards.put(monsterId, cardItemId);
                        }
                    }
            );

            System.out.println(
                    "[MonsterCardData] " +
                    cards.size() +
                    " carte(s) de monstre chargée(s)."
            );

        } catch (SQLException e) {
            super.sendError(e);
            Main.stop("Can't load monster cards");
        }
    }

    /**
     * Retourne l'ID de l'item correspondant à un monstre.
     *
     * @param monsterId identifiant du monstre
     * @return ID de l'item carte, ou -1 si aucune carte n'existe
     */
    public int getCardItemId(int monsterId) {
        return cards.getOrDefault(monsterId, -1);
    }
		/**
	 * Retourne l'identifiant du monstre correspondant à une carte.
	 *
	 * @param cardItemId identifiant de l'item carte
	 * @return monster_id ou -1 si aucune association n'existe
	 */
	public int getMonsterIdByCardItemId(int cardItemId) {
		for (Map.Entry<Integer, Integer> entry : cards.entrySet()) {
			if (entry.getValue() == cardItemId) {
				return entry.getKey();
			}
		}

		return -1;
	}

    /**
     * Indique si une carte existe pour ce monstre.
     */
    public boolean hasCard(int monsterId) {
        return cards.containsKey(monsterId);
    }

    /**
     * Permet de consulter toutes les associations chargées.
     *
     * La map retournée est en lecture seule afin d'éviter
     * qu'un autre morceau du serveur puisse la modifier
     * accidentellement.
     */
    public Map<Integer, Integer> getCards() {
        return Collections.unmodifiableMap(cards);
    }

    @Override
    public Object load(int id) {
        throw new NotImplementedException();
    }

    @Override
    public boolean insert(Object entity) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(Object entity) {
        throw new NotImplementedException();
    }

    @Override
    public void update(Object entity) {
        throw new NotImplementedException();
    }

    @Override
    public Class<?> getReferencedClass() {
        return MonsterCardData.class;
    }
}
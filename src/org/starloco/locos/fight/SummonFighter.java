package org.starloco.locos.fight;

import org.starloco.locos.client.other.Stats;
import org.starloco.locos.entity.monster.MonsterGrade;
import org.starloco.locos.kernel.Constant;
import org.starloco.locos.client.Player;
 
import org.starloco.locos.database.DatabaseManager;
import org.starloco.locos.database.data.game.MonsterCardData;
import java.util.HashMap;
import java.util.Map;

public class SummonFighter extends MobFighter {

    protected Fighter summoner;

    // Indique si cette invocation doit participer au calcul du butin
    // et transmettre les récompenses à son invocateur.
    private boolean lootForInvoker = false;

    protected SummonFighter(int id, Fight f, MonsterGrade mobGrade, Fighter summoner) {
        super(id, f, mobGrade);
        this.summoner = summoner;
    }

    // Active ou désactive la participation de l'invocation au butin.
    public void setLootForInvoker(boolean lootForInvoker) {
        this.lootForInvoker = lootForInvoker;
    }

    // Indique si cette invocation doit être prise en compte dans le calcul du butin.
    public boolean isLootForInvoker() {
        return this.lootForInvoker;
    }

    @Override
    public Stats getBaseStats() {

        // Pour une invocation appartenant à un joueur, ses caractéristiques
        // principales sont basées sur 10 % des statistiques du joueur.
        // Les PA, PM et résistances restent ceux du monstre invoqué.
        if (!(summoner instanceof PlayerFighter))
            return super.getBaseStats();

        PlayerFighter playerFighter = (PlayerFighter) summoner;

        // On part des statistiques natives du monstre.
        Map<Integer, Integer> stats =
                new HashMap<>(super.getBaseStats().getEffects());

        // On récupère les statistiques complètes du joueur pendant le combat.
        Stats playerStats = playerFighter.getTotalStats();

        // Coefficient demandé : 10 % des statistiques du joueur.
        // final double SUMMON_STAT_RATIO = 0.10D;

        // Vitalité minimale de l'invocation.
        // L'invocation possède toujours 50 PV de base auxquels on ajoute
        // 10 % de la Vitalité du joueur.
        final int BASE_SUMMON_VITALITY = 5;
		
		// PlayerFighter playerFighter = (PlayerFighter) summoner;
		Player player = playerFighter.getPlayer();

		MonsterCardData cardData =
			DatabaseManager.get(MonsterCardData.class);

		int monsterId = mobGrade.getTemplate().getId();

		int cardItemId =
			cardData.getCardItemId(monsterId);

		int cardCount =
			player.getNbItemTemplate(cardItemId);

		cardCount = Math.min(cardCount, 10);

		double statMultiplier =
			cardCount / 10.0D;
			 

        // Ces caractéristiques proviennent à 10 % du joueur.
        // Les PA, PM et résistances ne sont volontairement PAS modifiés :
        // ils restent ceux du MonsterGrade.
        int[] playerBasedStats = {
            Constant.STATS_ADD_SAGE,
            Constant.STATS_ADD_INTE,
            Constant.STATS_ADD_FORC,
            Constant.STATS_ADD_CHAN,
            Constant.STATS_ADD_AGIL,
            Constant.STATS_ADD_PROS,
            Constant.STATS_ADD_DOMA,
            Constant.STATS_ADD_SOIN,
            Constant.STATS_ADD_PERDOM,
            Constant.STATS_ADD_INIT
        };

        for (int stat : playerBasedStats) {
            int playerValue = playerStats.getEffect(stat);

            // L'invocation reçoit 10 % de la valeur du joueur.
            stats.put(
                stat,
                (int) Math.floor(playerValue * statMultiplier)
            );
        }
		
		if (cardCount >= 10) {

			    // Récupère la PO du personnage.
			int bonusPO = playerStats.getEffect(Constant.STATS_ADD_PO);

			if (bonusPO > 0) {
				// Ajoute la PO du personnage à la PO native de l'invocation.
				stats.put(
					Constant.STATS_ADD_PO,
					stats.getOrDefault(Constant.STATS_ADD_PO, 0) + bonusPO
				);
			}
			 
			// Récupère uniquement le bonus PA du personnage.
			int bonusPA = playerStats.getEffect(Constant.STATS_ADD_PA2);

			// Récupère uniquement le bonus PM du personnage.
			int bonusPM = playerStats.getEffect(Constant.STATS_ADD_PM2);

			// Ajoute uniquement le bonus PA aux PA natifs de l'invocation.
			if (bonusPA > 0) {
				stats.put(
					Constant.STATS_ADD_PA,
					stats.getOrDefault(Constant.STATS_ADD_PA, 0) + bonusPA
				);
			}

			// Ajoute uniquement le bonus PM aux PM natifs de l'invocation.
			if (bonusPM > 0) {
				stats.put(
					Constant.STATS_ADD_PM,
					stats.getOrDefault(Constant.STATS_ADD_PM, 0) + bonusPM
				);
			}
		}
		
		

        // Vitalité spéciale :
        // 50 PV de base + 10 % de la Vitalité du joueur.
        int playerVitality =
                playerStats.getEffect(Constant.STATS_ADD_VITA);

		int summonVitality =
				(BASE_SUMMON_VITALITY * cardCount)
				+ (int) Math.floor(playerVitality * statMultiplier);

        stats.put(Constant.STATS_ADD_VITA, summonVitality);

        return new Stats(stats);
    }
	
			@Override
		public int getPa() {
			int pa = super.getPa();

			if (!(summoner instanceof PlayerFighter))
				return pa;

			PlayerFighter playerFighter = (PlayerFighter) summoner;
			Player player = playerFighter.getPlayer();

			MonsterCardData cardData =
					DatabaseManager.get(MonsterCardData.class);

			int monsterId = mobGrade.getTemplate().getId();
			int cardItemId = cardData.getCardItemId(monsterId);

			int cardCount = player.getNbItemTemplate(cardItemId);

			// Bonus PA uniquement à partir de 10 cartes.
			if (cardCount >= 10) {
				Stats playerStats = playerFighter.getTotalStats();

				// PA du joueur moins ses 7 PA de base.
				int bonusPA =
						playerStats.getEffect(Constant.STATS_ADD_PA) - 7;

				if (bonusPA > 0)
					pa += bonusPA;
			}

			return pa;
		}

		@Override
		public int getPm() {
			int pm = super.getPm();

			if (!(summoner instanceof PlayerFighter))
				return pm;

			PlayerFighter playerFighter = (PlayerFighter) summoner;
			Player player = playerFighter.getPlayer();

			MonsterCardData cardData =
					DatabaseManager.get(MonsterCardData.class);

			int monsterId = mobGrade.getTemplate().getId();
			int cardItemId = cardData.getCardItemId(monsterId);

			int cardCount = player.getNbItemTemplate(cardItemId);

			// Bonus PM uniquement à partir de 10 cartes.
			if (cardCount >= 10) {
				Stats playerStats = playerFighter.getTotalStats();

				// PM du joueur moins ses 3 PM de base.
				int bonusPM =
						playerStats.getEffect(Constant.STATS_ADD_PM) - 3;

				if (bonusPM > 0)
					pm += bonusPM;
			}

			return pm;
		}
	
	
}
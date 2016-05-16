package graviton.game.exchange.job;

import graviton.game.client.player.Player;
import graviton.game.exchange.api.Exchange;
import graviton.game.job.actions.JobAction;
import graviton.game.job.actions.type.Craft;
import graviton.game.job.utils.CraftData;
import graviton.game.object.Object;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Botan on 15/06/2016.
 */
public class BreakerExchange implements Exchange {
    private final Player player;
    private final JobAction jobAction;
    private boolean stopCraft = false, isRepeat = false;
    private ScheduledExecutorService scheduler;

    private int objectCraft = 0;

    private Map<Short, Short> ingredients, lastIngredients;

    public BreakerExchange(Player player, JobAction jobAction) {
        this.player = player;
        this.jobAction = jobAction;
        this.ingredients = new HashMap();
        this.lastIngredients = new HashMap();
        this.scheduler = player.getGameManager().getJobScheduler();
    }

    public void repeatCraft(CraftData craftData, int repetition) {
        ingredients.keySet().forEach(object -> player.setObjectQuantity(player.getObjectByTemplate(object), player.getObjectByTemplate(object).getQuantity() - ingredients.get(object)));
        player.refreshPods();
        if (craftData != null) {
            objectCraft++;

            if ((repetition) - (objectCraft) == 0 || stopCraft) {
                finish(stopCraft);
                player.send("EcK;" + craftData.getResult());
            } else {
                player.send("EA" + (repetition - (objectCraft)));
                scheduler.schedule(() -> repeatCraft(craftData, repetition), 1, TimeUnit.SECONDS);
            }

            if (((Craft) jobAction).getChance() - new Random().nextInt(100) + 1 < 0) {
                player.send("EcEF");
                player.send("Im0118");
                player.getMap().send("IO" + player.getId() + "|-");
                scheduler.schedule(() -> repeatCraft(craftData, repetition), 1, TimeUnit.SECONDS);
                return;
            }

            player.addObject(player.getGameManager().getObjectTemplate(craftData.getResult()).createObject(1, false), true);
            player.send("EmKO+" + player.getObjectByTemplate(craftData.getResult()).getId() + "|1|" + craftData.getResult() + "|");
            player.getMap().send("IO" + player.getId() + "|+" + craftData.getResult());
        } else {
            player.send("Ea4");
            player.getMap().send("IO" + player.getId() + "|-");
        }
    }

    public void finish(boolean broken) {
        player.send(broken ? "Ea2" : "EA0\nEa1");
        stopCraft = false;
        isRepeat = false;
        objectCraft = 0;
        ingredients.clear();
    }

    public void craft(CraftData craftData) {
        if (isRepeat)
            return;

        ingredients.keySet().forEach(object -> player.setObjectQuantity(player.getObjectByTemplate(object), player.getObjectByTemplate(object).getQuantity() - ingredients.get(object)));
        player.refreshPods();

        if (craftData != null) {
            player.addObject(player.getGameManager().getObjectTemplate(craftData.getResult()).createObject(1, true), true);
            player.send("EmKO+" + player.getObjectByTemplate(craftData.getResult()).getId() + "|1|" + craftData.getResult() + "|");
            player.send("EcK;+" + craftData.getResult());
            player.getMap().send("IO" + player.getId() + "|+" + craftData.getResult());
        } else {
            player.send("EcEI");
            player.getMap().send("IO" + player.getId() + "|-");
        }
        this.ingredients.clear();
    }

    @Override
    public void cancel() {
        this.jobAction.stop(player, null);
    }

    @Override
    public void apply() {

    }

    @Override
    public void toogleOk(int id) {
        if (id < 0) {
            isRepeat = true;
            scheduler.schedule(() -> repeatCraft(((Craft) jobAction).get(ingredients), (id * -1) + 1), 1, TimeUnit.SECONDS);
        } else {
            this.lastIngredients.putAll(ingredients);
            scheduler.schedule(() -> craft(((Craft) jobAction).get(ingredients)), 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void addObject(int idObject, int quantity, int idPlayer) {
        int template = player.getObjects().get(idObject).getTemplate().getId();
        this.ingredients.put((short) template, (short) (this.ingredients.getOrDefault((short) template, (short) 0) + quantity));
        player.send("EMKO+" + idObject + "|" + this.ingredients.get((short) template));
    }

    @Override
    public void removeObject(int idObject, int quantity, int idPlayer) {
        int template = player.getObjects().get(idObject).getTemplate().getId();
        this.ingredients.put((short) template, (short) (this.ingredients.getOrDefault((short) template, (short) 0) - quantity));
        player.send("EMKO" + (this.ingredients.get((short) template) <= 0 ? "-" + idObject : "+" + idObject + "|" + this.ingredients.get((short) template)));
    }

    @Override
    public void editKamas(int idPlayer, long kamas) {

    }

    @Override
    public int getObjectQuantity(Player player, int id) {
        return 0;
    }

    public void setLastIngredients() {
        this.lastIngredients.keySet().forEach(ingredient -> {
            Object object = player.getObjectByTemplate(ingredient);
            if (object != null && (object.getQuantity() >= this.lastIngredients.get(ingredient)))
                addObject(object.getId(), this.lastIngredients.get(ingredient), -1);
        });
        this.lastIngredients.clear();
    }

    public void stopCraft() {
        this.stopCraft = true;
    }

}

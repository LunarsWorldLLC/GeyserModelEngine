package re.imc.geysermodelengine.managers.model.modelhandler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface ModelHandler {

    /**
     * Creates the model from the required Model Engine
     * @param objects Processes the required objects
     */
    void createModel(Object... objects);

    /**
     * Processes entities into createModel()
     * @param entity Registers bukkit entities
     */
    void processEntities(Entity entity);

    /**
     * Loads the required listeners
     */
    void loadListeners();

    default boolean canSee(Player player, Object model) {
        return true;
    }

    /**
     * Horizontal distance (blocks) the model should stay visible at for Bedrock
     * players when it is further than the view-distance based default;
     * 0 keeps the default.
     */
    default double viewDistance(Object model) {
        return 0;
    }
}

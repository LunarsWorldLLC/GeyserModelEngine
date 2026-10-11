package re.imc.geysermodelengine.managers.model.modelhandler;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import re.imc.geysermodelengine.GeyserModelEngine;
import re.imc.geysermodelengine.listener.ModelEngineListener;
import re.imc.geysermodelengine.managers.model.entity.EntityData;
import re.imc.geysermodelengine.managers.model.entity.ModelEngineEntityData;
import re.imc.geysermodelengine.managers.model.model.Model;
import re.imc.geysermodelengine.managers.model.model.ModelEngineModel;
import re.imc.geysermodelengine.managers.model.propertyhandler.PropertyHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModelEngineHandler implements ModelHandler {

    //TODO move driver hashmap here

    private final GeyserModelEngine plugin;

    public ModelEngineHandler(GeyserModelEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public void createModel(Object... objects) {
        ModeledEntity megEntity = (ModeledEntity) objects[0];
        ActiveModel megActiveModel = (ActiveModel) objects[1];

        int entityID = megEntity.getBase().getEntityId();

        PropertyHandler propertyHandler = plugin.getEntityTaskManager().getPropertyHandler();
        EntityData entityData = new ModelEngineEntityData(plugin, megEntity, megActiveModel);

        Model model = new ModelEngineModel(megActiveModel, this, entityData, propertyHandler);

        Map<Model, EntityData> entityDataCache = plugin.getModelManager().getEntitiesCache().computeIfAbsent(entityID, k -> new HashMap<>());

        for (Map.Entry<Model, EntityData> entry : entityDataCache.entrySet()) {
            if (entry.getKey() != model && entry.getKey().getName().equals(megActiveModel.getBlueprint().getName())) {
                return;
            }
        }

        plugin.getModelManager().getModelEntitiesCache().put(entityID, model);
        entityDataCache.put(model, entityData);

        if (plugin.getConfigManager().getConfig().getBoolean("options.debug.spawn")) plugin.getLogger().info("Creating model for " + model.getName());
    }

    @Override
    public void processEntities(Entity entity) {
        if (plugin.getModelManager().getEntitiesCache().containsKey(entity.getEntityId())) return;

        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(entity);
        if (modeledEntity == null) return;

        Optional<ActiveModel> model = modeledEntity.getModels().values().stream().findFirst();
        model.ifPresent(m -> createModel(modeledEntity, m));
    }

    // TODO ModelEngine canSee impl

    /**
     * Models on a ModelEngine Dummy (statues, stationary bosses) are seen as far
     * as the Dummy's render radius, the same radius Java players get. Models on
     * real entities keep the view-distance default.
     */
    @Override
    public double viewDistance(Object model) {
        if (!(model instanceof ActiveModel activeModel) || activeModel.getModeledEntity() == null) return 0;
        if (activeModel.getModeledEntity().getBase() instanceof Dummy<?> dummy) return dummy.getRenderRadius();
        return 0;
    }

    @Override
    public void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new ModelEngineListener(plugin), plugin);
    }
}

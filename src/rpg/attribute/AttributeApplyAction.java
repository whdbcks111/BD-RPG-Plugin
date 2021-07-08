package rpg.attribute;

import rpg.entity.Entity;

public interface AttributeApplyAction {
    void apply(Entity rpgEntity, double value);
}

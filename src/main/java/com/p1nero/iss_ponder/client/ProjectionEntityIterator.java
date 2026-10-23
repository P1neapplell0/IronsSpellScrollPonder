package com.p1nero.iss_ponder.client;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Stable tick traversal; removals affect the live level, additions wait until next tick. */
public final class ProjectionEntityIterator implements Iterator<Entity> {
    private final List<Entity> live;
    private final List<Entity> snapshot;
    private int index;
    private Entity last;

    public ProjectionEntityIterator(List<Entity> entities) {
        live = entities;
        snapshot = new ArrayList<>(entities);
    }

    @Override
    public boolean hasNext() {
        while (index < snapshot.size() && !live.contains(snapshot.get(index))) {
            index++;
        }
        return index < snapshot.size();
    }

    @Override
    public Entity next() {
        if (!hasNext()) throw new NoSuchElementException();
        last = snapshot.get(index++);
        return last;
    }

    @Override
    public void remove() {
        if (last == null) throw new IllegalStateException();
        live.remove(last);
        last = null;
    }
}

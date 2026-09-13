package org.neofabric.core;

import java.lang.reflect.Constructor;
import java.util.Objects;

/** Backend-neutral constructor bridge for loader-owned mod construction. */
public final class ModConstructorBridge {
    private ModConstructorBridge() {}

    public static <T> T construct(Class<T> modClass, Object eventBus) {
        Objects.requireNonNull(modClass, "modClass");
        try {
            if (eventBus != null) {
                for (Constructor<?> constructor : modClass.getDeclaredConstructors()) {
                    Class<?>[] parameters = constructor.getParameterTypes();
                    if (parameters.length == 1 && parameters[0].isInstance(eventBus)) {
                        constructor.setAccessible(true);
                        @SuppressWarnings("unchecked") T instance = (T) constructor.newInstance(eventBus);
                        return instance;
                    }
                }
            }
            Constructor<T> noArgs = modClass.getDeclaredConstructor();
            noArgs.setAccessible(true);
            return noArgs.newInstance();
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Unable to construct compatible mod " + modClass.getName(), error);
        }
    }
}

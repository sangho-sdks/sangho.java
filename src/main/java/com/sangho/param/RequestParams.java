package com.sangho.param;

import java.util.Map;

/** Paramètres de requête construits avec un {@code builder()} : le client HTTP les envoie sous la forme de {@link #toMap()}. */
public interface RequestParams {
    Map<String, ?> toMap();
}

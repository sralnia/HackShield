/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  lombok.NonNull
 */
package eu.okaeri.configs.serdes.standard;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.ObjectTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import java.math.BigDecimal;
import lombok.NonNull;

public class StringToShortTransformer
extends ObjectTransformer<String, Short> {
    @Override
    public GenericsPair<String, Short> getPair() {
        return this.genericsPair(String.class, Short.class);
    }

    @Override
    public Short transform(@NonNull String data, @NonNull SerdesContext serdesContext) {
        if (data == null) {
            throw new NullPointerException("data is marked non-null but is null");
        }
        if (serdesContext == null) {
            throw new NullPointerException("serdesContext is marked non-null but is null");
        }
        return new BigDecimal(data).shortValueExact();
    }
}


package com.trivadis.kafkaws.kstream.heterogenous;

import com.trivadis.kafkaws.avro.v1.Context;

public interface ContextProvider {
    public Context getContext();
}

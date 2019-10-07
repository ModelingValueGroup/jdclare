package org.modelingvalue.transactions;

@SuppressWarnings("serial")
public abstract class ConsistencyError extends RuntimeException {

    private final Object  object;
    private final Feature feature;

    protected ConsistencyError(Object object, Feature feature, String message) {
        super(message);
        this.object = object;
        this.feature = feature;
    }

    public Object getObject() {
        return object;
    }

    public Feature getFeature() {
        return feature;
    }

}

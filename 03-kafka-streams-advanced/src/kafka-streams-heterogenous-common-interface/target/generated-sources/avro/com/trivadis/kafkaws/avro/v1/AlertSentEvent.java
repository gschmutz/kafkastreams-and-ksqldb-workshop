/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.trivadis.kafkaws.avro.v1;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class AlertSentEvent extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord, com.trivadis.kafkaws.kstream.heterogenous.ContextProvider {
  private static final long serialVersionUID = 4976828293744979375L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"AlertSentEvent\",\"namespace\":\"com.trivadis.kafkaws.avro.v1\",\"fields\":[{\"name\":\"context\",\"type\":{\"type\":\"record\",\"name\":\"Context\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"logicalType\":\"uuid\"}},{\"name\":\"when\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}}]}},{\"name\":\"alert\",\"type\":{\"type\":\"record\",\"name\":\"Alert\",\"fields\":[{\"name\":\"alert\",\"type\":\"string\"},{\"name\":\"severity\",\"type\":\"string\"}]}}],\"java-interface\":\"com.trivadis.kafkaws.kstream.heterogenous.ContextProvider\"}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();
  static {
    MODEL$.addLogicalTypeConversion(new org.apache.avro.Conversions.UUIDConversion());
    MODEL$.addLogicalTypeConversion(new org.apache.avro.data.TimeConversions.TimestampMillisConversion());
  }

  private static final BinaryMessageEncoder<AlertSentEvent> ENCODER =
      new BinaryMessageEncoder<AlertSentEvent>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<AlertSentEvent> DECODER =
      new BinaryMessageDecoder<AlertSentEvent>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<AlertSentEvent> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<AlertSentEvent> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<AlertSentEvent> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<AlertSentEvent>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this AlertSentEvent to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a AlertSentEvent from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a AlertSentEvent instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static AlertSentEvent fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private com.trivadis.kafkaws.avro.v1.Context context;
  private com.trivadis.kafkaws.avro.v1.Alert alert;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public AlertSentEvent() {}

  /**
   * All-args constructor.
   * @param context The new value for context
   * @param alert The new value for alert
   */
  public AlertSentEvent(com.trivadis.kafkaws.avro.v1.Context context, com.trivadis.kafkaws.avro.v1.Alert alert) {
    this.context = context;
    this.alert = alert;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return context;
    case 1: return alert;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: context = (com.trivadis.kafkaws.avro.v1.Context)value$; break;
    case 1: alert = (com.trivadis.kafkaws.avro.v1.Alert)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'context' field.
   * @return The value of the 'context' field.
   */
  public com.trivadis.kafkaws.avro.v1.Context getContext() {
    return context;
  }


  /**
   * Sets the value of the 'context' field.
   * @param value the value to set.
   */
  public void setContext(com.trivadis.kafkaws.avro.v1.Context value) {
    this.context = value;
  }

  /**
   * Gets the value of the 'alert' field.
   * @return The value of the 'alert' field.
   */
  public com.trivadis.kafkaws.avro.v1.Alert getAlert() {
    return alert;
  }


  /**
   * Sets the value of the 'alert' field.
   * @param value the value to set.
   */
  public void setAlert(com.trivadis.kafkaws.avro.v1.Alert value) {
    this.alert = value;
  }

  /**
   * Creates a new AlertSentEvent RecordBuilder.
   * @return A new AlertSentEvent RecordBuilder
   */
  public static com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder newBuilder() {
    return new com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder();
  }

  /**
   * Creates a new AlertSentEvent RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new AlertSentEvent RecordBuilder
   */
  public static com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder newBuilder(com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder other) {
    if (other == null) {
      return new com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder();
    } else {
      return new com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder(other);
    }
  }

  /**
   * Creates a new AlertSentEvent RecordBuilder by copying an existing AlertSentEvent instance.
   * @param other The existing instance to copy.
   * @return A new AlertSentEvent RecordBuilder
   */
  public static com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder newBuilder(com.trivadis.kafkaws.avro.v1.AlertSentEvent other) {
    if (other == null) {
      return new com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder();
    } else {
      return new com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder(other);
    }
  }

  /**
   * RecordBuilder for AlertSentEvent instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<AlertSentEvent>
    implements org.apache.avro.data.RecordBuilder<AlertSentEvent> {

    private com.trivadis.kafkaws.avro.v1.Context context;
    private com.trivadis.kafkaws.avro.v1.Context.Builder contextBuilder;
    private com.trivadis.kafkaws.avro.v1.Alert alert;
    private com.trivadis.kafkaws.avro.v1.Alert.Builder alertBuilder;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.context)) {
        this.context = data().deepCopy(fields()[0].schema(), other.context);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (other.hasContextBuilder()) {
        this.contextBuilder = com.trivadis.kafkaws.avro.v1.Context.newBuilder(other.getContextBuilder());
      }
      if (isValidValue(fields()[1], other.alert)) {
        this.alert = data().deepCopy(fields()[1].schema(), other.alert);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (other.hasAlertBuilder()) {
        this.alertBuilder = com.trivadis.kafkaws.avro.v1.Alert.newBuilder(other.getAlertBuilder());
      }
    }

    /**
     * Creates a Builder by copying an existing AlertSentEvent instance
     * @param other The existing instance to copy.
     */
    private Builder(com.trivadis.kafkaws.avro.v1.AlertSentEvent other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.context)) {
        this.context = data().deepCopy(fields()[0].schema(), other.context);
        fieldSetFlags()[0] = true;
      }
      this.contextBuilder = null;
      if (isValidValue(fields()[1], other.alert)) {
        this.alert = data().deepCopy(fields()[1].schema(), other.alert);
        fieldSetFlags()[1] = true;
      }
      this.alertBuilder = null;
    }

    /**
      * Gets the value of the 'context' field.
      * @return The value.
      */
    public com.trivadis.kafkaws.avro.v1.Context getContext() {
      return context;
    }


    /**
      * Sets the value of the 'context' field.
      * @param value The value of 'context'.
      * @return This builder.
      */
    public com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder setContext(com.trivadis.kafkaws.avro.v1.Context value) {
      validate(fields()[0], value);
      this.contextBuilder = null;
      this.context = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'context' field has been set.
      * @return True if the 'context' field has been set, false otherwise.
      */
    public boolean hasContext() {
      return fieldSetFlags()[0];
    }

    /**
     * Gets the Builder instance for the 'context' field and creates one if it doesn't exist yet.
     * @return This builder.
     */
    public com.trivadis.kafkaws.avro.v1.Context.Builder getContextBuilder() {
      if (contextBuilder == null) {
        if (hasContext()) {
          setContextBuilder(com.trivadis.kafkaws.avro.v1.Context.newBuilder(context));
        } else {
          setContextBuilder(com.trivadis.kafkaws.avro.v1.Context.newBuilder());
        }
      }
      return contextBuilder;
    }

    /**
     * Sets the Builder instance for the 'context' field
     * @param value The builder instance that must be set.
     * @return This builder.
     */

    public com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder setContextBuilder(com.trivadis.kafkaws.avro.v1.Context.Builder value) {
      clearContext();
      contextBuilder = value;
      return this;
    }

    /**
     * Checks whether the 'context' field has an active Builder instance
     * @return True if the 'context' field has an active Builder instance
     */
    public boolean hasContextBuilder() {
      return contextBuilder != null;
    }

    /**
      * Clears the value of the 'context' field.
      * @return This builder.
      */
    public com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder clearContext() {
      context = null;
      contextBuilder = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'alert' field.
      * @return The value.
      */
    public com.trivadis.kafkaws.avro.v1.Alert getAlert() {
      return alert;
    }


    /**
      * Sets the value of the 'alert' field.
      * @param value The value of 'alert'.
      * @return This builder.
      */
    public com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder setAlert(com.trivadis.kafkaws.avro.v1.Alert value) {
      validate(fields()[1], value);
      this.alertBuilder = null;
      this.alert = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'alert' field has been set.
      * @return True if the 'alert' field has been set, false otherwise.
      */
    public boolean hasAlert() {
      return fieldSetFlags()[1];
    }

    /**
     * Gets the Builder instance for the 'alert' field and creates one if it doesn't exist yet.
     * @return This builder.
     */
    public com.trivadis.kafkaws.avro.v1.Alert.Builder getAlertBuilder() {
      if (alertBuilder == null) {
        if (hasAlert()) {
          setAlertBuilder(com.trivadis.kafkaws.avro.v1.Alert.newBuilder(alert));
        } else {
          setAlertBuilder(com.trivadis.kafkaws.avro.v1.Alert.newBuilder());
        }
      }
      return alertBuilder;
    }

    /**
     * Sets the Builder instance for the 'alert' field
     * @param value The builder instance that must be set.
     * @return This builder.
     */

    public com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder setAlertBuilder(com.trivadis.kafkaws.avro.v1.Alert.Builder value) {
      clearAlert();
      alertBuilder = value;
      return this;
    }

    /**
     * Checks whether the 'alert' field has an active Builder instance
     * @return True if the 'alert' field has an active Builder instance
     */
    public boolean hasAlertBuilder() {
      return alertBuilder != null;
    }

    /**
      * Clears the value of the 'alert' field.
      * @return This builder.
      */
    public com.trivadis.kafkaws.avro.v1.AlertSentEvent.Builder clearAlert() {
      alert = null;
      alertBuilder = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AlertSentEvent build() {
      try {
        AlertSentEvent record = new AlertSentEvent();
        if (contextBuilder != null) {
          try {
            record.context = this.contextBuilder.build();
          } catch (org.apache.avro.AvroMissingFieldException e) {
            e.addParentField(record.getSchema().getField("context"));
            throw e;
          }
        } else {
          record.context = fieldSetFlags()[0] ? this.context : (com.trivadis.kafkaws.avro.v1.Context) defaultValue(fields()[0]);
        }
        if (alertBuilder != null) {
          try {
            record.alert = this.alertBuilder.build();
          } catch (org.apache.avro.AvroMissingFieldException e) {
            e.addParentField(record.getSchema().getField("alert"));
            throw e;
          }
        } else {
          record.alert = fieldSetFlags()[1] ? this.alert : (com.trivadis.kafkaws.avro.v1.Alert) defaultValue(fields()[1]);
        }
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<AlertSentEvent>
    WRITER$ = (org.apache.avro.io.DatumWriter<AlertSentEvent>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<AlertSentEvent>
    READER$ = (org.apache.avro.io.DatumReader<AlertSentEvent>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}










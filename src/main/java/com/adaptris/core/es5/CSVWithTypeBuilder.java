package com.adaptris.core.es5;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.es5.types.ConfiguredTypeBuilder;
import com.adaptris.core.es5.types.TypeBuilder;
import com.adaptris.core.util.Args;

public abstract class CSVWithTypeBuilder extends CSVDocumentBuilderImpl {
  @NotNull
  @AutoPopulated
  private TypeBuilder typeBuilder;

  public CSVWithTypeBuilder() {
    setTypeBuilder(new ConfiguredTypeBuilder());
  }

  /**
   * @return the typeBuilder
   */
  public TypeBuilder getTypeBuilder() {
    return typeBuilder;
  }

  /**
   * @param typeBuilder the typeBuilder to set
   */
  public void setTypeBuilder(TypeBuilder typeBuilder) {
    this.typeBuilder = Args.notNull(typeBuilder, "TypeBuilder");
  }
}

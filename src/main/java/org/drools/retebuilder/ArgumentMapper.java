package org.drools.retebuilder;

import org.drools.core.common.InternalFactHandle;

public interface ArgumentMapper {

    Object getFact(InternalFactHandle[] factHandles);

    InternalFactHandle getFactHandle(InternalFactHandle[] factHandles);
}

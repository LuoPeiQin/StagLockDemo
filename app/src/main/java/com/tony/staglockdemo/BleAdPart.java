/*
 * Copyright (c) 2020. stag All rights reserved.
 */

package com.tony.staglockdemo;

import lombok.Data;

@Data
public class BleAdPart {
    private int length;
    private byte type;
    private byte[] values;
}

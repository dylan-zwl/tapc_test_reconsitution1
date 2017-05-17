package com.tapc.test.model.item;

import com.tapc.android.controller.MachineController;
import com.tapc.test.entity.Config;

public class ErpTest {
    public static void start() {
        MachineController.getInstance().enterErpStatus(5);
        if (!Config.IS_8935_PLATFORM) {
            System.exit(0);
        }
    }
}

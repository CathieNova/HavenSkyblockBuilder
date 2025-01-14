package net.cathienova.haven_skyblock_builder.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

public class DistUtils
{
    public static void runIfOn(Dist dist, Supplier<?> supplier) {
        if (FMLEnvironment.dist == dist) {
            supplier.get();
        }
    }
}
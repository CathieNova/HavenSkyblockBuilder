package net.cathienova.haven_skyblock_builder.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemplateUtil
{
    public static CompoundTag readTemplate(Path path) throws IOException, CommandSyntaxException
    {
        return TemplateUtil.readTemplate(path, path.toString().endsWith(".snbt"));
    }

    public static CompoundTag readTemplate(Path path, boolean snbt) throws IOException, CommandSyntaxException {
        if (snbt) {
            return NbtUtils.snbtToStructure(IOUtils.toString(Files.newBufferedReader(path)));
        } else {
            return NbtIo.read(path);
        }
    }
}

package com.lying.neoforge;

import net.neoforged.fml.common.Mod;

import com.lying.Reclamation;
import com.lying.reference.Reference;

@Mod(Reference.ModInfo.MOD_ID)
public final class ReclamationNeoForge
{
    public ReclamationNeoForge()
    {
        // Run our common setup.
        Reclamation.init();
    }
}

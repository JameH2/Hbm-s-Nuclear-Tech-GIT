//=======================================================
//Mod Client File
//=======================================================
package com.hbm.main;

import java.util.Arrays;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

import com.google.common.eventbus.EventBus;
import com.hbm.lib.RefStrings;

@MCVersion(value = "1.7.10")
public class NTMCore extends DummyModContainer
{
	@Instance("ntm_coremod")
	public static NTMCore instance;

	@SidedProxy(clientSide = RefStrings.CLIENTSIDE, serverSide = RefStrings.SERVERSIDE)
	public static ServerProxy proxy;


	public NTMCore()
	{
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = "ntm_coremod";
		meta.name = "NTM[coremod]";
		meta.version = RefStrings.VERSION;
		meta.credits = "";
		//meta.authorList = Arrays.asList("Bioxx", "Dunkleosteus");
		meta.description = "";
		//meta.url = "www.terrafirmacraft.com";
		//meta.updateUrl = "";
		//meta.screenshots = new String[0];
		meta.logoFile = "";
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		instance = this;

	}

	@EventHandler
	public void initialize(FMLInitializationEvent evt)
	{

	}

	@EventHandler
	public void modsLoaded(FMLPostInitializationEvent evt) 
	{

	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent evt)
	{

	}	

}

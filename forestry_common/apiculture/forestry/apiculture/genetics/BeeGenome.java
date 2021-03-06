/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.apiculture.genetics;

import net.minecraft.nbt.NBTTagCompound;

import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.genetics.EnumTolerance;
import forestry.api.genetics.IAlleleFloat;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IAlleleInteger;
import forestry.api.genetics.IChromosome;
import forestry.api.genetics.IFlowerProvider;
import forestry.api.genetics.ISpeciesRoot;
import forestry.core.genetics.AlleleArea;
import forestry.core.genetics.AlleleBoolean;
import forestry.core.genetics.AlleleTolerance;
import forestry.core.genetics.Genome;
import forestry.core.utils.Vect;
import forestry.plugins.PluginApiculture;

public class BeeGenome extends Genome implements IBeeGenome {
	/**
	 * 0 - Species (determines product)
	 * 
	 * 1 - Speed
	 * 
	 * 2 - Lifespan
	 * 
	 * 3 - Fertility (Maximum number of offspring)
	 * 
	 * 4 - Preferred temperature Icy: Snow biomes Cold: Tundra/Steppe, Extreme Mountains/Hills? Normal: Plains, Forests, Mountains Hot: Desert Hellish: Nether
	 * 
	 * 5 - Temperature tolerance (Range +/-)
	 * 
	 * 6 - Nocturnal
	 * 
	 * 7 - Preferred humidity (Arid - Normal - Damp)
	 * 
	 * 8 - Humidity tolerance (Range +/-)
	 * 
	 * 9 - Flight interference tolerance (stuff falling from the sky/other hindrances -> tolerates dampness + flight interference tolerance => rain resistance)
	 * 
	 * 10 - Cave dwelling
	 * 
	 * 11 - Required flowers
	 * 
	 * 12 - Flower plant chance
	 * 
	 * 13 - Territory
	 */
	/* CONSTRUCTOR */
	public BeeGenome(NBTTagCompound nbttagcompound) {
		super(nbttagcompound);
	}

	public BeeGenome(IChromosome[] chromosomes) {
		super(chromosomes);
	}

	// / INFORMATION RETRIEVAL
	@Override
	public IAlleleBeeSpecies getPrimary() {
		return (IAlleleBeeSpecies) getActiveAllele(EnumBeeChromosome.SPECIES.ordinal());
	}

	@Override
	public IAlleleBeeSpecies getSecondary() {
		return (IAlleleBeeSpecies) getInactiveAllele(EnumBeeChromosome.SPECIES.ordinal());
	}

	@Override
	public float getSpeed() {
		return ((IAlleleFloat) getActiveAllele(EnumBeeChromosome.SPEED.ordinal())).getValue();
	}

	@Override
	public int getLifespan() {
		return ((IAlleleInteger) getActiveAllele(EnumBeeChromosome.LIFESPAN.ordinal())).getValue();
	}

	@Override
	public int getFertility() {
		return ((IAlleleInteger) getActiveAllele(EnumBeeChromosome.FERTILITY.ordinal())).getValue();
	}

	@Override
	public EnumTolerance getToleranceTemp() {
		return ((AlleleTolerance) getActiveAllele(EnumBeeChromosome.TEMPERATURE_TOLERANCE.ordinal())).getValue();
	}

	@Override
	public boolean getNocturnal() {
		return ((AlleleBoolean) getActiveAllele(EnumBeeChromosome.NOCTURNAL.ordinal())).getValue();
	}

	@Override
	public EnumTolerance getToleranceHumid() {
		return ((AlleleTolerance) getActiveAllele(EnumBeeChromosome.HUMIDITY_TOLERANCE.ordinal())).getValue();
	}

	@Override
	public boolean getTolerantFlyer() {
		return ((AlleleBoolean) getActiveAllele(EnumBeeChromosome.TOLERANT_FLYER.ordinal())).getValue();
	}

	@Override
	public boolean getCaveDwelling() {
		return ((AlleleBoolean) getActiveAllele(EnumBeeChromosome.CAVE_DWELLING.ordinal())).getValue();
	}

	@Override
	public IFlowerProvider getFlowerProvider() {
		return ((IAlleleFlowers) getActiveAllele(EnumBeeChromosome.FLOWER_PROVIDER.ordinal())).getProvider();
	}

	@Override
	public int getFlowering() {
		return ((IAlleleInteger) getActiveAllele(EnumBeeChromosome.FLOWERING.ordinal())).getValue();
	}

	@Override
	public int[] getTerritory() {
		Vect area = ((AlleleArea) getActiveAllele(EnumBeeChromosome.TERRITORY.ordinal())).getArea();
		return new int[] { area.x, area.y, area.z };
	}

	@Override
	public IAlleleBeeEffect getEffect() {
		return (IAlleleBeeEffect) getActiveAllele(EnumBeeChromosome.EFFECT.ordinal());
	}

	@Override
	public ISpeciesRoot getSpeciesRoot() {
		return PluginApiculture.beeInterface;
	}
}

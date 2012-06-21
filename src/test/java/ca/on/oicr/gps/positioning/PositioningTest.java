package ca.on.oicr.gps.positioning;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.*;

public class PositioningTest {
	
	private GenePositionLocator positioning;
	
	@Before
    public void setUp() {
		positioning = new GenePositionLocator();
    }

	@Test
	public void testFindPositioning1() {
		
		GeneReference ref = new GeneReference("NM_006218.1", "c.1624G>A");
		List<GeneReference> refs = new ArrayList<GeneReference>();
		refs.add(ref);
		
		positioning.translateReference(refs);
		
		assertTrue(ref.getStart() != -1);
		assertTrue(ref.getStop() != -1);
		assertTrue(ref.getChromosome() != null);
		assertTrue(ref.getVarAllele() != null);
		assertEquals(178936082, ref.getStart());
		assertEquals(178936082, ref.getStop());
		assertEquals("3", ref.getChromosome());
	}

	@Test
	public void testFindPositioning2() {
		
		GeneReference ref = new GeneReference("NM_000141", "c.1647T>G");
		List<GeneReference> refs = new ArrayList<GeneReference>();
		refs.add(ref);
		
		positioning.translateReference(refs);
		
		assertTrue(ref.getStart() != -1);
		assertTrue(ref.getStop() != -1);
		assertTrue(ref.getChromosome() != null);
		assertTrue(ref.getVarAllele() != null);
		assertEquals(123258034, ref.getStart());
		assertEquals(123258034, ref.getStop());
		assertEquals("10", ref.getChromosome());
		assertEquals("G", ref.getVarAllele());
	}

	@Test
	public void testFindPositioning3() {
		
		GeneReference ref = new GeneReference("NM_004333", "c.1761C>G");
		List<GeneReference> refs = new ArrayList<GeneReference>();
		refs.add(ref);
		
		positioning.translateReference(refs);
		
		assertTrue(ref.getStart() != -1);
		assertTrue(ref.getStop() != -1);
		assertTrue(ref.getChromosome() != null);
		assertTrue(ref.getVarAllele() != null);
		assertEquals(140453174, ref.getStart());
		assertEquals(140453174, ref.getStop());
		assertEquals("7", ref.getChromosome());
		assertEquals("G", ref.getVarAllele());
	}

	@Test
	public void testFindPositioning4() {
		
		GeneReference ref = new GeneReference("NM_004333", "c.1405_1406GG>TC");
		List<GeneReference> refs = new ArrayList<GeneReference>();
		refs.add(ref);
		
		positioning.translateReference(refs);
		
		assertTrue(ref.getStart() != -1);
		assertTrue(ref.getStop() != -1);
		assertTrue(ref.getChromosome() != null);
		assertTrue(ref.getVarAllele() != null);
		assertEquals(140481402, ref.getStart());
		assertEquals(140481403, ref.getStop());
		assertEquals("7", ref.getChromosome());
		assertEquals("TC", ref.getVarAllele());
	}

	@Test
	public void testFindPositioning5() {
		
		GeneReference ref = new GeneReference("NM_004333", "c.1457_1471del15");
		List<GeneReference> refs = new ArrayList<GeneReference>();
		refs.add(ref);
		
		positioning.translateReference(refs);
		
		assertTrue(ref.getStart() != -1);
		assertTrue(ref.getStop() != -1);
		assertTrue(ref.getChromosome() != null);
		assertTrue(ref.getVarAllele() != null);
		assertEquals(140477837, ref.getStart());
		assertEquals(140477851, ref.getStop());
		assertEquals("7", ref.getChromosome());
		assertEquals("-", ref.getVarAllele());
	}
}

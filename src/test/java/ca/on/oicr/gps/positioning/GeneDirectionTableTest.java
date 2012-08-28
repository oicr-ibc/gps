package ca.on.oicr.gps.positioning;

import static org.junit.Assert.*;

import org.junit.Test;

public class GeneDirectionTableTest {

	@Test
	public void testGetInstance() {
		GeneDirectionTable table = GeneDirectionTable.getInstance();
		
		assertNotNull(table);
	}

	@Test
	public void testTableContents() {
		GeneDirectionTable table = GeneDirectionTable.getInstance();
		
		assertEquals("-", table.getDirection("NM_002916"));
		assertEquals("+", table.getDirection("NM_015106"));
		assertEquals("+", table.getDirection("NM_001260504"));
		assertEquals("-", table.getDirection("NM_001258418"));
	}
}

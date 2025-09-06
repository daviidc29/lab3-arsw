/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blueprints.test.persistence.impl;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.persistence.impl.InMemoryBlueprintPersistence;

import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hcadavid
 */
public class InMemoryPersistenceTest {
    
    @Test
    public void saveNewAndLoadTest() throws BlueprintPersistenceException, BlueprintNotFoundException{
        InMemoryBlueprintPersistence ibpp=new InMemoryBlueprintPersistence();

        Point[] pts0=new Point[]{new Point(40, 40),new Point(15, 15)};
        Blueprint bp0=new Blueprint("mack", "mypaint",pts0);
        
        ibpp.saveBlueprint(bp0);
        
        Point[] pts=new Point[]{new Point(0, 0),new Point(10, 10)};
        Blueprint bp=new Blueprint("john", "thepaint",pts);
        
        ibpp.saveBlueprint(bp);
        
        assertNotNull("Loading a previously stored blueprint returned null.",ibpp.getBlueprint(bp.getAuthor(), bp.getName()));
        
        assertEquals("Loading a previously stored blueprint returned a different blueprint.",ibpp.getBlueprint(bp.getAuthor(), bp.getName()), bp);
        
    }


    @Test
    public void saveExistingBpTest() {
        InMemoryBlueprintPersistence ibpp=new InMemoryBlueprintPersistence();
        
        Point[] pts=new Point[]{new Point(0, 0),new Point(10, 10)};
        Blueprint bp=new Blueprint("john", "thepaint",pts);
        
        try {
            ibpp.saveBlueprint(bp);
        } catch (BlueprintPersistenceException ex) {
            fail("Blueprint persistence failed inserting the first blueprint.");
        }
        
        Point[] pts2=new Point[]{new Point(10, 10),new Point(20, 20)};
        Blueprint bp2=new Blueprint("john", "thepaint",pts2);

        try{
            ibpp.saveBlueprint(bp2);
            fail("An exception was expected after saving a second blueprint with the same name and autor");
        }
        catch (BlueprintPersistenceException ex){
            
        }
                
        
    }

    @Test
    public void getBlueprintShouldThrowWhenNotFound() throws  BlueprintNotFoundException {
        InMemoryBlueprintPersistence ipbb = new InMemoryBlueprintPersistence();
        ipbb.getBlueprint("no-author","no-blueprint");

    }

    @Test
    public void getBlueprintsByAuthorShouldReturnOnlyAuthorsBlueprints() throws Exception {
        InMemoryBlueprintPersistence ibpp = new InMemoryBlueprintPersistence();
        Blueprint j1 = new Blueprint("john", "p1", new Point[]{new Point(1, 1)});
        Blueprint j2 = new Blueprint("john", "p2", new Point[]{new Point(2, 2)});
        Blueprint m1 = new Blueprint("mack", "m1", new Point[]{new Point(3, 3)});
        ibpp.saveBlueprint(j1);
        ibpp.saveBlueprint(j2);
        ibpp.saveBlueprint(m1);
        Set<Blueprint> johns = ibpp.getBlueprintsByAuthor("john");
        assertEquals("Author 'john' should have 2 blueprints", 2, johns.size());
        assertTrue(johns.contains(j1));
        assertTrue(johns.contains(j2));
        Set<Blueprint> macks = ibpp.getBlueprintsByAuthor("mack");
        assertEquals("Author 'mack' should have 1 blueprint", 1, macks.size());
        assertTrue(macks.contains(m1));
        Set<Blueprint> nobody = ibpp.getBlueprintsByAuthor("nobody");
        assertNotNull(nobody);
        assertTrue("Unknown author should return empty set", nobody.isEmpty());
    }

    @Test
    public void getAllBlueprintsShouldReturnUnion() throws Exception {
        InMemoryBlueprintPersistence ibpp = new InMemoryBlueprintPersistence();
        int initial = ibpp.getAllBlueprints().size();
        Blueprint a = new Blueprint("a", "a1", new Point[]{new Point(0, 0)});
        Blueprint b = new Blueprint("b", "b1", new Point[]{new Point(1, 1)});
        Blueprint c = new Blueprint("b", "b2", new Point[]{new Point(2, 2)});
        ibpp.saveBlueprint(a);
        ibpp.saveBlueprint(b);
        ibpp.saveBlueprint(c);
        assertEquals(initial + 3, ibpp.getAllBlueprints().size());
    }

    
}
 
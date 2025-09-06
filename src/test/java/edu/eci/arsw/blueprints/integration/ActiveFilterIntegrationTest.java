package edu.eci.arsw.blueprints.integration;

import static org.junit.Assert.*;

import edu.eci.arsw.blueprints.config.AppConfig;
import edu.eci.arsw.blueprints.filters.BlueprintFilter;
import edu.eci.arsw.blueprints.filters.RedundancyFilter;
import edu.eci.arsw.blueprints.filters.SubsamplingFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class ActiveFilterIntegrationTest {

    @Autowired private BlueprintsServices services;
    @Autowired private BlueprintFilter activeFilter; // Será el bean @Primary

    @Before
    public void seed() {
        // Datos con los que distinguimos filtros
        Point a = new Point(0,0);
        Point b = new Point(1,1);
        Blueprint dups = new Blueprint("john","dups",
                new Point[]{ a,a,a,b,b,new Point(2,2) }); // para redundancia vs submuestreo
        Blueprint lng  = new Blueprint("mike","long",
                new Point[]{ new Point(0,0), new Point(1,1), new Point(2,2),
                             new Point(3,3), new Point(4,4), new Point(5,5) });
        services.addNewBlueprint(dups);
        services.addNewBlueprint(lng);
    }

    @Test
    public void behaviorDependsOnPrimaryFilter() throws Exception {
        if (activeFilter instanceof RedundancyFilter) {
            assertRedundancyBehavior();
        } else if (activeFilter instanceof SubsamplingFilter) {
            assertSubsamplingBehavior();
        } else {
            fail("Filtro activo desconocido: " + activeFilter.getClass());
        }
    }

    private void assertRedundancyBehavior() throws BlueprintNotFoundException {
        // john:dups -> a,b,(2,2) => 3 puntos (sin repetidos consecutivos)
        Blueprint dupsOut = services.getBlueprint("john","dups");
        assertEquals(3, dupsOut.getPoints().size());
        assertPt(dupsOut, 0, 0,0);
        assertPt(dupsOut, 1, 1,1);
        assertPt(dupsOut, 2, 2,2);

        // mike:long -> no hay duplicados => 6 puntos
        Blueprint longOut = services.getBlueprint("mike","long");
        assertEquals(6, longOut.getPoints().size());
    }

    private void assertSubsamplingBehavior() throws BlueprintNotFoundException {
        // john:dups (a,a,a,b,b,2,2) -> índices 0,2,4 -> a,a,b
        Blueprint dupsOut = services.getBlueprint("john","dups");
        assertEquals(3, dupsOut.getPoints().size());
        assertPt(dupsOut, 0, 0,0);
        assertPt(dupsOut, 1, 0,0); // sigue a
        assertPt(dupsOut, 2, 1,1); // b

        // mike:long (0..5) -> 0,2,4 => 3 puntos
        Blueprint longOut = services.getBlueprint("mike","long");
        assertEquals(3, longOut.getPoints().size());
        assertPt(longOut, 0, 0,0);
        assertPt(longOut, 1, 2,2);
        assertPt(longOut, 2, 4,4);
    }

    private static void assertPt(Blueprint bp, int idx, int x, int y) {
        assertEquals(x, bp.getPoints().get(idx).getX());
        assertEquals(y, bp.getPoints().get(idx).getY());
    }
}

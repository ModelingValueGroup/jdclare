package org.modelingvalue.jdclare.swing.examples.newton;

import static java.lang.Math.*;
import static org.modelingvalue.jdclare.DClare.*;

import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;

public interface BallPair extends DStruct2<Ball, Ball>, DObject {

    @Property(key = 0)
    Ball a();

    @Property(key = 1)
    Ball b();

    @Property
    default double collisionTime() {
        DPoint va = pre(a(), Ball::velocity);
        DPoint vb = pre(b(), Ball::velocity);
        if (!va.equals(DPoint.NULL) || !vb.equals(DPoint.NULL)) {
            Table table = a().table();
            DPoint pa = pre(a(), Ball::position);
            DPoint pb = pre(b(), Ball::position);
            DPoint dv = va.minus(vb);
            DPoint dp = pa.minus(pb);
            double a = dv.dot(dv);
            double b = 2 * dp.dot(dv);
            double c = dp.dot(dp);
            double d = pow(b, 2.0) - 4 * a * (c - 4 * table.ballRadiusPow());
            if (d >= 0) {
                double sqrt = sqrt(d);
                double t1 = (-b + sqrt) / (2 * a);
                double t2 = (-b - sqrt) / (2 * a);
                return min(t1, t2);
            }
        }
        return Double.MAX_VALUE;
    }

    @Property
    DPoint aVelocityDelta();

    @Property
    DPoint bVelocityDelta();

    @Rule
    default void collision() {
        Table table = a().table();
        if (equals(table.collision())) {
            DPoint va = a().solVelocity();
            DPoint vb = b().solVelocity();
            DPoint na = b().solPosition().minus(a().solPosition()).normal();
            DPoint nb = na.mult(-1.0);
            DPoint vna = na.mult(va.dot(na));
            DPoint vnb = nb.mult(vb.dot(nb));
            DPoint vta = vna.minus(va);
            DPoint vtb = vnb.minus(vb);
            double f = 1.0 - table.ballsBouncingResistance();
            DPoint va_ = vta.plus(vnb).mult(f);
            DPoint vb_ = vtb.plus(vna).mult(f);
            set(this, BallPair::aVelocityDelta, va_.minus(va));
            set(this, BallPair::bVelocityDelta, vb_.minus(vb));
        } else {
            set(this, BallPair::aVelocityDelta, DPoint.NULL);
            set(this, BallPair::bVelocityDelta, DPoint.NULL);
        }
    }

}

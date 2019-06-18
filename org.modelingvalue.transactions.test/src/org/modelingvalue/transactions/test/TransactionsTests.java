//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018 Modeling Value Group B.V. (http://modelingvalue.org)                                             ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the "License"). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Contributors:                                                                                                       ~
//     Wim Bast, Carel Bast, Tom Brus, Arjan Kok, Ronald Krijgsheld                                                    ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.transactions.test;

import static java.math.BigInteger.*;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.ContextThread;
import org.modelingvalue.collections.util.ContextThread.ContextPool;
import org.modelingvalue.transactions.Constant;
import org.modelingvalue.transactions.Observed;
import org.modelingvalue.transactions.Observer;
import org.modelingvalue.transactions.Setable;
import org.modelingvalue.transactions.State;
import org.modelingvalue.transactions.UniverseTransaction;

public class TransactionsTests {

    static final BigInteger                       TWO       = BigInteger.valueOf(2);

    static final Constant<BigInteger, BigInteger> FIBONACCI = Constant.of("FIBONACCI", n -> {
                                                                if (n.equals(ZERO) || n.equals(ONE)) {
                                                                    return n;
                                                                } else {
                                                                    return TransactionsTests.FIBONACCI.get(n.subtract(ONE)).add(   //
                                                                            TransactionsTests.FIBONACCI.get(n.subtract(TWO)));
                                                                }
                                                            });

    static final ContextPool                      THE_POOL  = ContextThread.createPool();

    @Test
    public void source2target() throws Exception {
        Observed<DUniverse, DObject> child = Observed.of("child", null, true);
        Observed<DObject, Integer> source = Observed.of("source", 0);
        Setable<DObject, Integer> target = Setable.of("target", 0);
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe", child));
        DClass dClass = DClass.of("Object", Observer.of("observer", o -> target.set(o, source.get(o))));
        DObject object = DObject.of("object", dClass);
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL);
        universeTransaction.put("step1", () -> child.set(universe, object));
        universeTransaction.put("step2", () -> source.set(object, 10));
        universeTransaction.stop();
        State result = universeTransaction.waitForEnd();
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
        Assert.assertEquals(10, (int) result.get(object, target));
    }

    @Test
    public void cycle1second() throws Exception {
        Observed<DUniverse, Long> currentTime = Observed.of("time", System.currentTimeMillis());
        long begin = System.currentTimeMillis();
        Observed<DUniverse, Set<DObject>> children = Observed.of("children", Set.of(), true);
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe", children));
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL, 100, r -> currentTime.set(universe, System.currentTimeMillis()));
        DClass dClass = DClass.of("Object", Observer.of("observer", o -> {
            long time = currentTime.get(universe);
            if (time - begin > 1000) {
                UniverseTransaction.STOPPED.set(universe, true);
            }
        }));
        universeTransaction.put("step1", () -> {
            for (int io = 0; io < 8; io++) {
                children.set(universe, Set::add, DObject.of(io, dClass));
            }
        });
        State result = universeTransaction.waitForEnd();
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
        long end = result.get(universe, currentTime);
        long duration = (end - begin) / 1000;
        System.err.println(duration + " s");
        System.err.println("********************************************************************");
    }

    @Test
    public void derivationChain() throws Exception {
        Observed<DUniverse, Set<DObject>> children = Observed.of("children", Set.of(), true);
        Observed<DObject, Integer> number = Observed.of("number", 0);
        Observed<DObject, Integer> total = Observed.of("total", 0);
        int length = 30;
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe", children));
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL);
        DClass dClass = DClass.of("Object", Observer.of("observer", o -> {
            int i = (int) o.id();
            total.set(o, number.get(o) + (i > 0 ? total.get(DObject.of(i - 1, o.dClass())) : 0));
        }));
        universeTransaction.put("step1", () -> {
            for (int io = 0; io < length; io++) {
                children.set(universe, Set::add, DObject.of(io, dClass));
            }
        });
        universeTransaction.put("step2", () -> {
            for (int io = 0; io < length; io++) {
                number.set(DObject.of(io, dClass), 1);
            }
        });
        universeTransaction.stop();
        State result = universeTransaction.waitForEnd();
        Assert.assertEquals(length, (int) result.get(DObject.of(length - 1, dClass), total));
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
    }

    static final Observed<DObject, DObject> next     = Observed.of("next", null, () -> TransactionsTests.previous);
    static final Observed<DObject, DObject> previous = Observed.of("previous", null, () -> TransactionsTests.next);

    @Test
    public void opposites() throws Exception {
        Observed<DUniverse, Set<DObject>> children = Observed.of("children", Set.of(), true);

        int length = 30;
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe", children));
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL);
        DClass dClass = DClass.of("Object");
        universeTransaction.put("backwards", () -> {
            for (int i = 0; i < length; i++) {
                DObject o = DObject.of(i, dClass);
                children.set(universe, Set::add, o);
                next.set(o, i > 0 ? DObject.of(i - 1, dClass) : null);
            }
        });
        universeTransaction.put("forwards", () -> {
            for (int i = 0; i < length; i++) {
                DObject o = DObject.of(i, dClass);
                previous.set(o, i > 0 ? DObject.of(i - 1, dClass) : null);
            }
        });
        universeTransaction.stop();
        State result = universeTransaction.waitForEnd();
        Assert.assertEquals(DObject.of(11, dClass), result.get(DObject.of(10, dClass), next));
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
    }

    @Test
    public void moveAndRemove() throws Exception {
        Observed<DObject, Set<DObject>> children = Observed.of("children", Set.of(), true);
        Observed<DObject, String> name = Observed.of("name", null);
        Observed<DObject, String> qualifiedName = Observed.of("qualifiedName", null);
        DClass dClass = DClass.of("Object", children, //
                Observer.of("qualifiedName", o -> qualifiedName.set(o, qualifiedName.get(o.dParent(DObject.class)) + "." + name.get(o))), //
                Observer.of("name", o -> name.set(o, (String) o.id())));
        DObject c1 = DObject.of("c1", dClass);
        DObject c2 = DObject.of("c2", dClass);
        DObject gc1 = DObject.of("gc1", dClass);
        DObject gc2 = DObject.of("gc2", dClass);
        DObject gc3 = DObject.of("gc3", dClass);
        DObject gc4 = DObject.of("gc4", dClass);
        DObject ggc1 = DObject.of("ggc1", dClass);
        DObject ggc2 = DObject.of("ggc2", dClass);
        DObject ggc3 = DObject.of("ggc3", dClass);
        DObject ggc4 = DObject.of("ggc4", dClass);
        DObject ggc5 = DObject.of("ggc5", dClass);
        DObject ggc6 = DObject.of("ggc6", dClass);
        DObject ggc7 = DObject.of("ggc7", dClass);
        DObject ggc8 = DObject.of("ggc8", dClass);
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe", children));
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL);
        universeTransaction.put("step1", () -> {
            qualifiedName.set(universe, "u");
            children.set(universe, Set.of(c1, c2));
            children.set(c1, Set.of(gc1, gc2));
            children.set(c2, Set.of(gc3, gc4));
            children.set(gc1, Set.of(ggc1, ggc2));
            children.set(gc2, Set.of(ggc3, ggc4));
            children.set(gc3, Set.of(ggc5, ggc6));
            children.set(gc4, Set.of(ggc7, ggc8));
        });
        universeTransaction.put("step2", () -> {
            children.set(c2, Set.of(gc1, gc2));
        });
        universeTransaction.stop();
        State result = universeTransaction.waitForEnd();
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
        Assert.assertEquals(Set.of(), result.get(c1, children));
        Assert.assertEquals(null, result.get(ggc6, qualifiedName));
        Assert.assertEquals("u.c2.gc2.ggc3", result.get(ggc3, qualifiedName));
    }

    @Test
    public void constants() throws Exception {
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe"));
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL);
        universeTransaction.put("step1", () -> FIBONACCI.get(BigInteger.valueOf(10)));
        universeTransaction.put("step2", () -> FIBONACCI.get(BigInteger.valueOf(100000)));
        universeTransaction.stop();
        State result = universeTransaction.waitForEnd();
        result.run(() -> {
            Assert.assertEquals(BigInteger.valueOf(6765), FIBONACCI.get(BigInteger.valueOf(20)));
            Assert.assertEquals("2f9p715e13jocebg0mi9o1jqbp7l2e52m0r6qx0hs8lwci1leebul8qsd7i1hytbwkxsn5nnctt1bzccjke806frfxa47jhwgl1vi3zr54f63h5fvoubyf7dbvx0j4swtpm34yj4ykikwkggwn00dbojcjv2wybb6kpiei560mcdnf5bli5grcpe01rxf3efk9uvu8g3g7qt0npvq0y3kbf5bosukopbsc0pk5htqlvobm0qoegtu2puvqd0cbawovwxtbf8hjtlwutyi9gclf8wytqysoo52c2nft8kbsse5k1shklir1j644lbhnjz7pht9pvm76ewwkw5jetxriunuhfjonm1ns6sdjhzih7shym47ikopu07c4ns9rvjn2k0a220piyt45eaxh10h9dwxnzllejfwazvhrd8koxhf5p7oxtv8wegufvixi4wbrln0l5rwp1299dern7coulwy3xcck1oih87snh3acj4m790dgd8jmd8372p6r26rdmqa3xkt9razxg8qg2it6wsgreihr23s4e7nfxrl6wm8wm43zm1liupyn2zvqg5i0fooxm8uf4ulmy2qmlvidu6fvs3ncn4vjz20vamiphxcys8lc3czkfqkar3m0iryicxhz9gtlvlb0gle441phb6nyo77vo6ovs70o1dtp8ehc4znc860i7ggqltxj6d61n1pklc7vfzz4fdxv3j9gyszv01vfy9l6xbvxtx2d991xlujcmwerirfc7pzyuqr64b8mv1v2ffrc9a8dly475jef8ukcwligcrmlamef4s5st0zmna63xl8qsl8y1o4bk08pced3h4edpu624q0qwsoa8ncu880m30w79cu49ptyeex04b3heh0dwd7gewxti5b80bfjljvap13pr380c90xhon4mn4gf2wjrzfdqoa68bb8ptxbm13tr33c5lvwb8n84zrpbajf6v1bhus2d3ber0ps9aeffin0c9jlfbe2nhgylhafs77jrgmnfj0zgfkgjjyq5gpeq7alczy4wyz1unrwgc39gbgls25zd3tstddvfdmc8d41bs3qeqvvmqpux78jjem1uo9nmg7gtl5331ny6t29t74tlidjtyxmhand3626jktwllsykmokgzavo6dhw7rf55ylujbzbfto5raghs3bpajc199dux0srscdtugluf4scyxet25y59roi2ghptoz22y217em1nkk53stdbi6pmahxn94g8he0iobcth70avvx5e1tf1b87x5t8psv6vf440kmdg8ahu5431d55892n3i27vy1xytuxnh8jk1tzyx9ejvgwh3z52q7delclng03kkuiv3a9jqqbe9ien3m1m25w5fdijzijpi1f0myfmtovr3c8rf73u1765slyiigxealhcasuziw6jgkzl4g7709i2u507mvkgrlkwehi2dmnmqa82py7u0y1vi61bnrspflkflvva92y5oeb0jxr69r0guec7g8zaz5gq3pr674au5mezkqeh5ox2hhji4g9ql6qct999vxqq6elwaibbd6qdhlfukg4bpyomp84u6he1veehn8ja360e6b65he2povrerwvn214l2y7t9hsv9rscytn9hjeok7s3nxgxmmatmvd3chpxr7s1u5tq0s01b5j4kyxjz5l9owit20btfl73vq21wi59ohc0c8jvm6rvd58ezp45a6zg33xahny1jp06fnm8spq70w3cegydvqn57s3w041etm7avjih7tp13rzd20oflucxhj1hvtcjifzahtog94aqo89m6ociuwtapkg8pxq1xuegyujhk4trjcnf0qatjsw6vwzwsmycmzgkxzv4gl0lm5jwq7ioe1sjmmuext08d2r7qw6487o97sw2ih4xbpnw21cfxukj1rqfad3t7tkdx1ymbog3slzagcvmfqiyxew1r9t4hafs0ito8i0zzujkp553nnb1cfiwmrpeq9j6zwp0a9naq3p1cs7nmke0fsnevvvvigpws6eh5ipnascbyhwve8zxy4zel3842aru0vmx1m5b5gz3nnm7qse42nzq6ixzpok4ucmxu6av1w9oj1vwtvyyg2229t75e91vyna8gndwyi7doxnm4ifn4rfkyhw325n30l61xoehc12qys30mpkxfjvb7wig7yph7i34aoscmm4za6bsqml9ddnwijkc9p3zmpfuui9oqxdtogqtg8fl897rm6xvhazkge4s055aqb87ca5cslrzp2htqwpout7qh5jg69atk8vveopum2e1cudauai5ajb7fwsvkbexiyobne2lk66nqsdku431due30b0rboibe0e2xnlcmku39xa0ei9gsfd8y3scqmhgptpppgi685gtub8ddbvr0a1fvxso8q96aj29lh68red0vdb907jfzgtmqwj196tt7xitmkiroliziddn6hvqpuqdrn7h5r30ylzzppp6ojt47n98kbj2mo9qoztllus9y9jzfztlrbkx5qbmdh47nc34s0b24s1pos843kis0bpx4vyucf6xamtolbtmz1sn9pbrdqrif0xcfdo0icw32i5ol7m6c56ttz7n9f0gu642l3ad0eet3nhmlow20jvhgesc97v49nbk4nfeteb37xij5vygzhaw8cmprez73tlxx9b0vubb6jum9hh3an0f7vahibjbtr75w5cixwbgs8ci618o4mdwavho17aufhje75284opawlobfvpaa4mxkn2uexnf8axksp3qe0d148zknat04l3qvxzbbkvajgz5r2lxc0y1r0nhy6p2hsjk0ik60yw9628il4kghkwvkmal5ncr3r5dthfl096cmk5o5ixgriod5suvhrnbfu4chk8bzk1m9wu09asu9qieo2h1ks81uh9o1ui97ybxki4ebk888lnq8g0panfxfit4bih943euxt4z6gmepz8nawupl5l3nyrttlu2usallk5vesf531vfuk0c7wd55ow0z7ku32cu83f90ymeacddvkbj4jmek2fhbt7hm20j1nrs1akyvka0wn4yuerq0sh9lsutfqhk9qwcmd30c9dv3e837rpb56ee201nyks45xuk4drfg17ejpky5zfeivhm1wer6lup8z7wo3f5tvwm13fa6ixbl78ua92nhzaawcxw0o5pnhew8rsshup9psflhjfxpb4ey5sap8abskehmliokplb56klhammic534rdkkqijj230qh64i6swi1o5at5zk17s980dsou8i2rhsilruuukcy17mj4b7z8z9na2purhz1yvraldhw3u9jntw71rxeljs4d5q32ptkckwo9nrl0rjw2usj9etmonpfy3a2fjv253khlgoba0ijh2gq9qjx8x59969kjosyjsr8y1u0r37udsiyuw5dzatqtuxf2mv86lt4mydcp23lezbhf2e5sgpwm9trha17ks64vyiijtc1iu9ge6xogkycblkfh36xl16griu753d2p7d83k3p85fi4bb1dgm30mqfkp9vjyftwbjcfuzhvegeig9k3taus6hqx6bkn2444i2izy00bwrnakfnupcumd8r9gxa7ei0yo7wqwm23rq5r2l9z5yt8w9d2z517nk6xb4q9nm2f5pxz1zwymycn7q1pl5jyelxrc3bo5ao31694wbur0vgtofeaa3i6lutg44kmha7b8fwt0rxelxi49x1vdq0v56xbbutxc16bkv8xe36sv6nk8lhpi5ga6lq5nnp9a0euvd2rar1tsriwbsj8btugumhjgr8vp7yrw6wpsq1fh1mh8j2nqzf7c50b1ctulh9k7w6rgt8xtk842mawnuz6w4bhuwbzn2k08rp6shrv7xx5943oyhhfhtvpdj2kndoramf24675hi8674hyp9edi7mr9meq60u1tzqt5pfctttmvbkehqmtb6cm6lhjmlfakhkmys911bn9tsgg685pkuiads27qyjlmu92bdbwvtcekigmcemj9euuyz4j364fikyfebjh7yo0983q9k6yiwi4rb2jth3lpokvkuhthfhlssm7o9hyvben0k64etzigbmubr7bip62cafkhnvfh39t0ogrfxf718pwypjhqyiaad9w3juje7gh33phlm7l3w8xvcqlp7i2lmx0cpxxur3lkg26ruxmya6qzdigh22piz4raxrivndddib9wn5tqr1tmsk4sdeu8w1j32c7fix3f9a3u6ve0bryrqu0l2slvknafy003qva4jag8lx1p1usctgbmo1c3wbgjfq217lbbsqa9813n3zbogl0vubqbohs3h997ko8d1301idizoaw0fqp99emlrcq3ypars2rpb7bywdlo01f7v58s40hgu0gry492xmdi2l7jfzdyrcgmd04hvmotgry282yot4hw8vl5rptce7fvatb4mikkwiel6vefhgupxc2kvk5d8uoofytzvj6kybwn3sn7kr66pgz8275h524abmg69taro4khwrgry9u5ktno5ezdjabr73792k0sikszsoju6wemd4y9om3aazfzntsav6yk2eor0z6qvar5hpz3mmkuw2ooqjubq33oh0k19mq1rnoq2r47wb8wu6fzhxw6r2ursyy7o235gyb8295xcduw21txttve00uynnod65nbkz31oqywiybon1o284pgforz0bv6xd6g1fwsssvkywxs6dggtofdcyzbe590l8jxmhoex2mdtmfuo49x54n2uh8kc2g4tuzffo423g5f60579cgd9x9mcdfqjgde88n5dwy87orxhnj8ok8zfmowfbyrstruf5lsod6rga8o990mzxc20oisaa28ktph4s6657pyoxupxhgabskf28nglha6hfbubx7mnucwgobu6d7zn61ciq6k7ffsju662wbl8b6g6ddsthszib1245yt30rog0q8icawsnz7a57zqb2m7ksz82vgv5wk4mk6ds8isko93q5zjyvz7o5uef4ulqm1xl0ds4k82pqpu34r6y151cue3gs5d3lua2c2x87wuwcoajbmn3x890isa09qs1f3u1gg6mp0yw5deqfbni8ab5yp933v4tqby4emeh6wlb9m8tq99lgijy8w4sdse83tpw1lcs6hfuqjtiqaogj68ozygo5k41sxssmcwygf8lic02p8tcwj66alokrvmvawpg9da67dcr4m4f2s7b7zguvt3za1sumnn69i2nzhwu3a9l56cudku04m7ex35mms69tfbkglqudvm0ez0pt0u44nranl36uy54lmzfd0wekm4xyx9h8v1apgyjjbsdgez310z6h6pt5ysssemliebkium8658jckc672w6k3i6qku4gxpmvlympa9ff2d17f8v41hbud8vx3z0y8xir6uimw2h4yksbrcv7uacb59ugoql6elliggd34a0zz4jrojt7mwsa81cz5pmo5ptya9w5vnr9oj9svswdn27pk6cxr0kynpndp1fw5bdhrgskuznfgihrhrwkk1dkfmrjkzor7bm07imhesz3hgbzdt18f4il5jlpkpvegdtvuk4p8imhp9shkzepauy2bxmsdfowsfhovgfeh7oysj00gny71rc03ju7vo4b6dykax1smq28yoygruxic3r5k1cx5ea30j0g8fo79n06rglghyfvzuwlew4dk5aievtnjieshxgdkm9h5s5wie2p5916z18mvyz0sqlttak9tyfxmj44h90ng2yow8kfv6vq9kyze9ftmme9yqzi7i85bky6xwv9shijo1eorwwnkev0eky5qf2rn7kw556ie3t3qetabtty8x1ur6ri2pqvaxn0bk6jfusuq2e64g3h7jwp1v2dbkmef75dr1pwgjqa1og7o8raf0td4n4vs8jyay6cvgnzerecsj9ybtiqiou4c8g8ssqe7omif03pakx525mtwlo8o3gu7co3q57yo7asxvgojxlihq2tviqil58pns9hxuufr7scpuiaxzsfbh2cbpw8sqzmkz2lw7svbe8wi1oushaemicfm25d6uaomq3i1tgjxiqvu97tw9mhq1p9p8i4yw6xwnjev6w9cigzo0w7vpyazln5uspnpvr30lk9mqn231ufj4n2dhe27g1fgksq0fxzpewfe5y9yfens5k8e87hb4qruqi367u0zkispkjmgfpxds5r8nqjj2f2muf81iw5st37svvauromwp3sb7ir0v47ksp9nhcvnwj3g59dsul8a6qryj3t0quf15kkadx858gbm6a1u7r5hk6791z2te43ir6y2hbsh8j3196s7a0tawmcvlvp6xfx35yxsbkfngc60k1qinr42l6tio783qs58tr2vr3c2shk4v3jnclhnk74p9ixzy67m0mfx5mhkk661qmyvewj6kuqow3p7zeeu8zskph6u19b5r9n2kcg7sw3sv1aixte02dtab1rd3bt2dr9uoq8ipk27r7mj6tqsjhatljlg7rz546e5cgavxfamg55juei130ibccyu00a9524smtnul6v9762ihlufxl0m9x16him36bm4d59steu3shku2d4f23juid2cusvgtsv1kg0hcz9bf8ej9n8v30epm7cb1zkp7zwc15u7k0l476i0dga2p0psfjff7q2o7s3tzvhes8c6ttdnn04cyhlnlya38vyeq1w3dgq770bjqhrb4i6oxvdn9a693479jx1lhdag7vpmvsemwh2ei8qlh8cvxr5hldaclyn8ilbz4d1tf1dssgdlr7ms1oxnfc374kuwdlvwulyg51g07cbxa20hx885yg6g2ij147t1ic1wd67h8kqbpafwx5jkbivwyo8af6z85pihjo713u1aji639eh42xalhzyxtax9i962timpbo7i37vuhgk37lad8aau3wuyfks17zaafmjvykwql15k9xfro1emwny5dbfu7zhantrz684wsh9sweb09064pv4h3r2murc05y9ov43ts8zurb8fjzskoal3qofi7shfgzoodxu7h6u7qcu0nhzwh1rbugb7qpzi5plj8g98pzke507y56xdb6rjytgmlu5zydnbim9rglp0t0t448truqmqgd0hj0qxv0q1uvk6bhc24ki7fa66mce1k973560kmpvbcax3zj5hyj4pzno1m1rny5qpqpvz3g2zl4gsk3g5cdlhg1btszieemoa7mz2el5kdv0c9vkmtbdkhg8e1yggs1801l48utscv1x9z5ys5hbyamwcoitk1c8z6wbwox8oebzydvq1tnvtpudxm2cadx7hxalexlpvoc6yznfdugfbbaembmp2i2om3cfhl8f87puofaco1iv27qo8vf8rsltqz9h0lx2hacvrcoryzw0ws40bepvmbf2mdgjklv4jy0hn1c7ybaylf056b8j5d2i0x8r3unkmdktzx6rn154y6rpn1a26uh9ome2ylroytpq3p3pxln4pxtgc46x4zzbg13vtmfmz9r5msecoh7l5yz8qjl9p6at4nnm34ibsukl69ow489gjc22180rez0m13d16len5dawu3mr1zw78w767fkfgih3md1o37gc19oh6yq0h9swcq88rc76mj9k22e8epdsjln31kg9yr6pmvbggvmovgdmw0nw321e14r4uh5gxjaxao2csy74w11py4aju2lspcild1z0nt5lpws373g2m4qigk794juse3ucodz4bzs6w25zgcd0t765ipgm3iv34x7n03svb845wu66f7dxf72vu6xzv1owdda58xsgc4op07xbgih055xon7t2xri8y6pqs3xpapikks3ljf3b2122yngb987p9imefaqqewhakkrws8rjpdqkfdv4afqnctohkxtyai6zgmh8mgpthy9g7827avxkhe9c28a244h04x1usgh788bj9nrsabhea6yrh05czu7zu8y98iqcs2pbwmjzib123prbs8l9ktkwwmjzdq8muqzsru8do5r75af0ethv29sxk2rvfj1qt0doo1xvdy6o0blgv3qunav8y7z4qs0ufj9f8zecvfuhrwu9s7p795jmdzuyh48svo8exb9e4snujb3d23d2d4kmm2or655vwmx93w3obxfmwquvot86pedofbevzja17ykxzelhi754bwu1mi7kfobu8b6yj9xlw2stm6q44lni6jbwrp0ko2r48vgq772co39pv7m81njcnlcp0uy76c9idvt7kygtrid0suivee9il57oackjjj9d7vkuc54bqd02sku5jncl8c86kiimi610d0wp07ec9uxe204784oqo2dyykp6ropdgyfwuqwlmqr7orltmczhk6exm2keyr631avn59qas3jnmv16bbofmhfv03bpsar51tlsyyqw7cecb0qlirftsk1839zn9il6lmfsh7qt90sasgbaqe73kfc9juh20ov257gdprxqke1jtfrjsi79quj2sh9tbr1twsp8mf6vzh58l2rcezwsmths2t53uj550u45tq9ogmd9ljhb0ctexd8iultgdpbi89mv0oth2dmdwe2luoy7fjpmu581xcrx64zxjfhuqi8o3ifp9btka880h012lt2hg0dg6kphdobpsqijg0h7gg2k9h415hh3oca0d04t7ps2ndvlhbx1y746qxe1vfi4vajoqsbbd2lj92b9ihba3gfsik81ig3z5oo4s0a45hy7nifq91lp64lo3aylnfh80mdysp0zi79zubq7nylspsds4sohggpv6p73ghu7hxxvk6779hwdh92hvb95qoi0stdg08yzvnatepkdax3xzempu8vh1fgus987ecjpptoq5e4sbpup0szvzjxt8gfp40cw6gth6ddijt5xnui8xces809eknlllolooiongzij5in493zqbk9g1434wzougs51nsutkk38s82h3tk9p0qq6lgjeingfxn7m9oxfkmmjv19uvf0ve1fqcddf495m8q8tjx6vc7205302ueoddefnwruj6ufswv2dxtjkjjvdvi1iqy52p5xjvp8p2rs9tddcsa1bsngijzsziq3ir5tvd0jj7oapsnnscvhckb5ciub4ltw1m1d6ov4pso0ips99xdbsueevanpkw6s4li46dt3kuozmmlci3k1ms1440bg1e41woqrv7jpqcu1el3lxxkqliu0616zjsfa5gdzb0rl92vhsfl2ag8cq16pttfbw7b0cbla4vv3rtxiojxe69du4mcd1g36dmadmp0eg44v5jtxu08aaynhtkd4gf5k079vryar9my63a5h1tteksozlqy36aa5mmr1gvprfhpdth1vl1r3sofqk3iasnt6f6ant8142ktreh3pc48km57z93qeuokd5fthvd2t0epdzwlrj6t9jnpl8dei2xwgyl5vi7nmc4tcbe8r0urkduw32ukcttucaa8ey2d6s37ny1f3dqnzhijkf4q0ujp8qa5hfncl4bdxq12e10omiiv86wtpqnuyn3tfqx31c1cfshjvgqbk10s564nw8qtt6yqab5pm78jtal4ds6o7zjhkdc8zoto5122jq3wij7v2lxg8y1g4aqiimvlo8bqfjbyxjm751w3r97ra7rdixgaitrprkk16g6hmnhgvgufx9o62v7t2f92r9tmaayqpii452g32y2usb2jpy1o3e4fbo1n1w00w2v4so0hcdh2co1t63tkpqo782azxsq39wux6knb002z81agp8lef64ueg4kdbp63uigicz1a6ocj7vdo9ai880h9tndv5uqaauouvkyniufut7jzmboxiqfs31ol76fogwp59ag4sdu7554rlss618u3bwhpmeb9e7m2p0x8cjvjl9226dq9gwk4g7m9754tfhqa25h8mhy0rtjebdecykm5dolci86y3lgs07pgb3ppvhsv4zvqynt89j74vlp8u8xqtnffdubnbt2i8muxjrrzwdu3kwj4t2aiuk37wid0r917sl58xb0obeal8h4ecrul23uxnz4zqv11kmgrt81wmxocuedurk1zjomtilq85lus2wyc7b4ak956yyxe32bjeh3gbxb1aj5vlah0mtau6mb0kfxuwjl6h4uqsoucow42giakinst76xivh9c7h46pbw5cymsbqf1x4ts4coamq2zx4jsiwwrpvzbt97ct0efp2rpqbsdiyv0tla3whz9ccuc7h268punb17tydfdhs7uisnrosay1lttu53advbg2wgumy1t5iz4mmc93k4g0pohagp4v6x42msxzyjfrm0u9fr9ov4r7kdjeoyqdsf9y9t714e5od7gffh0jj71vf13vgmq6ufagsgeiemajm3btc0vwy87v3zh6q58nu5126r7gazdz3ltcxr0bzqu15851qyppaadtgrqy7usjuzx4zfv5axp79oxmecx965kmnfxbi5h19xb2qd69lnd3zutt0zzcsuaw2sr0sxpv71t4i2sfledjiyw01p4m9v5r7ofz2qcf61ymso34ysu74lhsh31r0vi2fwi7gh2zm7ff3fqmqsyu1x9torq19hgykym659hcw979uw4hlsqpnqey8289x0296y0mvtxa8auoaz7svy354hjd48a6rm4ucue0h7dm0ec4k7b1jyvbl1tfwonu4hgq9h6hitzxqoyrcbfhgsjmqxq4cgebk5zn69bpwcfxmlovyt5uigedm4dwf9wseqczjhvkzxm355vq69z5ewtsi8i7tpvenp996l9ky34w18y5ngjoc5dv09t448oczcx7syxxf4n76sg9cskpb127ssq8lm4vp7211n8183axkp8cjnj8rfsylf5pbftrqs2i8hxo1x3xqw63iqvvm5c4cq0w1gxf4zj9oiyb0wq4zc0wcqozki7lp0qus54vdnzs6glvhxbldmw73yr3dsdbisk5fyjk5868smudmpxxqu5i8l54dotirpsojgjhs4mc0bnj50jkpgo6yx9xtgcw3emhraw07xwmve3v1y3f68f23zzq3y6ytse9b0zo4s3v8kdyw7mzxhe8x87yx291ijehzhfk48zshlnm9q3geo7ueieh0e6bm3uyir22dfnv5smje76pgdkqxvf8ig08s3f9njtwn49mxvyn46ntpp5tb6ym85s3quwaqaczkjoncyul0saqpl7k86sd3xz2do703s5l1zfpv6472ept24mligbptvuyinxw5srmkspc9uyk5psuobv9bo83oc6dx54dcg8d61o37zkuedtyp81pmdbzwp47z8ntb7qo6bnbsoif1lwy6yq1er2jly91owh2p2ieeiscknnuo97z0e2d2rd9ia8p25dm1ga3ejtyz3b2aeefuxp56s7yh8f1r1nbbk98cnwtzgm4treil92xege43vv1zbi26xy1hrrqe1odvkcl9ogcs9nlgqfqvp08h1dv41hjfid1oskx8vy8zmmsk8z9b172ivalsx65ucqtx7s2jjjnbzrt9j42lw1gn3bxlmvg9smnx10owe6ytvgga79lfsh8r2iiog0ij6p2bvhq6jitvvmwd18mj4t39pp2ntq3ejbr1yysddjvyg9go60bhwo2bdchh7xm3d9x0ps2w5eikyn0rdn8cysapb5c6tnzybsnpp5l3rykml1l6qdmerq5x0o7hv947ke2uznztt0th9pmgrm90qqfyma8dhimggaa1ky6jhoeoifrzlwrhpl489pxy2i5tw6lemmukqmsibomy9nj8n1omtv1cwi6dluaszzkc7wyi8zdso3rijunwqa3nffyga8hxpizjty3xuwg3xqoj27u3xum3o3gu0o5ggyqsq4lwznojks450ugt038xdwd52c8h1vtmv62hqj1xgtgqh7qqn8d5iovis952jjvvu8sxwlwqx5zuvt5niltats2yxhs0cti4omu2bs8f2e6kyuoftsqggo14qiad4hdo6stn8dz9r632tcssfw89km5kq0v3vlei885usnm70noscjlvmdzal3qn2c4lvwm4p23rqu12qsrln783bg8s9lrvwz4quiaab8dhj9kxbblrrswssonug2c2l68yj2xa8fvrmc2oacxcrtvd2if6cosu8ddy1qbw06foo8nc3sa370hb75kiovwp9m0piyx0py17ulptiyebujjash9dd70xx975b3qvdqqodqes2daz1hie4gc1vsu3iv5etbx5llgfx0ap24690uszli3px88wgcwwdbyvl1fdlicwtuhq3671z6nsn60bsde1z00shxd69lcw87u8lw8ajo0ukq5nnrkucduqnx0vacleruat6sxm9zjk4tjteced1foek63vcol7487pbsrkqa387zhadeotow9z0qdw65uarasu29xkuy6544stvrrglxsdm9td9ugyj3i91inzy29db62w4z89pf1qd4wp8jxezfe889i9v25jwtlelxnmutktsje4gplr1kl4iwws4phl8bvrc42l5ftk578ody8he2jcp3lzwuj43vl3iol8mo1hohm4pu12wqzokmzb2u25defs5fbx705u5fvuj94rp7z1zgn8yaafy1tibo5kvkzyyyv806o0dl9l65f0l3tr008bsko0vwnacrveh54r00yrnpq3v06do6cu4768zdzp49qoozy5g4f9acs8nf2a4rfkx6ncxz4t6lddvartlkc0wj907mp8bhbnle1rbjmkj5fyvzkfc0rojee8p6ilx3ejhni883xsvjwmfzbk9g42zcm2nntaaux8bjzvwnhc8ffuyg2ci88wza08z9q7rtuauga7qhdizikqepy08pstw9r4u9lt5nnoltyp0oauf7cqqq5kn8uywa3q68kzbo5569gk9wiazt42tnwul0k8h36bs7rxo9juzr9mvf90y5uuq9fjcq0v93qxdsffq3k86skivqja7c13yn1d1q232dq8f2ulalwzn1kr6tak70uuykzby2mfx14f7ah7h7q30g6bgge9ww7r59yz80jgf68osvcnc4ywidkhxn860r1queokkdupbiy5t3m2apg4fq6bvh201e0cboj0qptmry5fx44ijv0q1m41frjnepfol2l2rgflxysjtfs5wxxxs2ti3xtt0xe24t0zt5m4ix3pkq2wzppsgvjsp0gola327jcb8dfxjugghnl7ma3rqgif1xlxn0qn9n9n0lee7cu2c4tl6wa29zp1bv9i0zf", //
                    FIBONACCI.get(BigInteger.valueOf(100000)).toString(Character.MAX_RADIX));
        });
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
    }

}

package xxx.cas;

import xxx.utils.SingularAttributeExtension;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import xxx.model.cas.db.*;
import xxx.model.cas.repository.*;
import xxx.model.cas.repository.projections.ActivationEssentials;
import xxx.model.cas.repository.projections.SimReplacementEssentials;
import xxx.model.kie.db.ProcessInstanceLog_;
import xxx.model.kie.db.VariableInstanceLog;
import xxx.model.kie.db.VariableInstanceLog_;
import xxx.model.kie.repository.VariableInstanceLogRepository;
import xxx.utils.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.apache.commons.collections.ComparatorUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static xxx.utils.CriteriaUtils.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional

@ExtensionMethod({SingularAttributeExtension.class, SetAttributeExtension.class})
public class TestSearchOO {


    @Autowired
    SimReplacementRepository simReplacementRepository;

    @Autowired
    ActivationRepository activationRepository;

    @Autowired
    SubscriptionTypeSimAssociationRepository subscriptionTypeSimAssociationRepository;

    @Autowired
    VariableInstanceLogRepository variableInstanceLogRepository;

    @PersistenceContext
    EntityManager em;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ActivationInitialOrdersRepository activationInitialOrdersRepository;

    @Test
    public void contextLoads() {


        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Tuple.class, new TupleSerializer());
        objectMapper.registerModule(module);


//        CriteriaBuilder builder = em.getCriteriaBuilder();
//        CriteriaQuery<ActivationEssentials> cq = builder.createQuery(ActivationEssentials.class);
//// write the Root, Path elements as usual
//        Root<ActivationEssentials> root = cq.from(ActivationEssentials.class);
//        cq.multiselect(root.get("id"));  //using metamodel
//        List<ActivationEssentials> result = em.createQuery(cq).getResultList();

        //  SpelAwareProxyProjectionFactory

        //  Session session = em.unwrap(Session.class);
//        List<T> results = session.createCriteria(ActivationEssentials.class)
//                .setProjection( Projections.projectionList()
//                        .add( Property.forName("ID") )
//                        .add( Property.forName("VERSION") )
//                )
//                .setResultTransformer(Transformers.aliasToBean(entityClazz);


        String imsi1 = "420050000005393";

        Filter equal = Activation_.imsi.equal(imsi1);
        // Filter filter = Activation_.dealerCode.equal(dealerCode).and(Activation_.createdOn.greaterThanOrEqualTo(dtFrom)).and(Activation_.createdOn.lessThanOrEqualTo(dtTo));


        Filter findActivationORSimreplacmentByimsi1 = Activation_.imsi.equal(imsi1).and(Activation_.simReplacements.leftJoin(SimReplacement_.imsi.equal(imsi1)));

        List<Tuple> allByColumn36 = activationRepository.filter(findActivationORSimreplacmentByimsi1).findAll(Brand_.id, Brand_.name);

        System.out.println("Joins with condition:\n" + objectMapper.valueToTree(allByColumn36).toPrettyString());




        //{
        //    "filter": "createdOn < '2021-04-05 16:54:00'  & join('identityMaster','','') & join('subscriptionType','','')",
        //    "columns": [
        //       "as(dealerCode,`Requester`)","as(imsi,`imsi`)","as(msisdn,`msisdn`)","as(identityMaster.idNumber,`idNumber`)", "as(subscriptionType.brandId,`brandId`)",
        //       "as(if(subscriptionType.prepaid=true,1,2),`processOrderType`)",
        //       "as(createdOn,`date`)"
        //    ],
        //    "size": 1
        //}

        //Hibernate: select identityma1_.IdNumber as col_0_0_, activation0_.CreatedOn as col_1_0_, case when subscripti2_.IsPrepaid=? then 1 else 2 end as col_2_0_ from activation activation0_ inner join identitymaster identityma1_ on activation0_.IdentityMasterId=identityma1_.Id inner join subscriptiontype subscripti2_ on activation0_.SubscriptionTypeId=subscripti2_.Id where 1=1 and 1=1 and activation0_.CreatedOn<? limit ?

        Query<Activation> query5 = activationRepository.filter(
                Activation_.createdOn.lessThan(LocalDateTime.parse("2021-04-05T16:54:00"))
                        .and(Activation_.identityMaster.join())
                        .and(Activation_.subscriptionType.join()

                        )
        );

        //Optional<Tuple> allByColumn45 = query5.findFirst(IdentityMaster_.idNumber.as("iD Number"), Activation_.createdOn.as("date")
        // Hibernate: select substring(identityma1_.IdNumber, 1, 1) as col_0_0_, activation0_.CreatedOn as col_1_0_, case when subscripti2_.IsPrepaid=? then 1 else 2 end as col_2_0_ from activation activation0_ inner join identitymaster identityma1_ on activation0_.IdentityMasterId=identityma1_.Id inner join subscriptiontype subscripti2_ on activation0_.SubscriptionTypeId=subscripti2_.Id where 1=1 and 1=1 and activation0_.CreatedOn<? limit ?
//        Optional<Tuple> allByColumn45 = query5.findFirst(substring(IdentityMaster_.idNumber.to(String.class),1,1), Activation_.createdOn.as("date")
//                , as(ifthen(SubscriptionType_.prepaid.equal(true), 1, 2), "processOrderType"));


//        Optional<Tuple> allByColumn45 = query5.findFirst(substring(IdentityMaster_.idNumber.to(String.class),1,1), Activation_.createdOn.as("date")
//                , as(ifthen(SubscriptionType_.prepaid.equal(true), "1", SubscriptionType_.accountType), "processOrderType"));

        Optional<Tuple> allByColumn45 = query5.findFirst(as(ifthen(SubscriptionType_.prepaid.equal(true), "2", SubscriptionType_.accountType), "processOrderType"));
        allByColumn45 = query5.findFirst(as(ifthen(SubscriptionType_.prepaid.equal(true), SubscriptionType_.brandId.to(String.class), "2"), "processOrderType"));

// Optional<Tuple> allByColumn45 = query5.findFirst(substring(IdentityMaster_.idNumber.to(String.class),1,1), Activation_.createdOn.as("date")
//                , as(ifthen(SubscriptionType_.prepaid.equal(true), "1", SubscriptionType_.prepaid.to(String.class)), "processOrderType"));


        Tuple tuple = allByColumn45.get();
        System.out.println("Cols only by attrib & IF THEN:\n" + objectMapper.valueToTree(tuple).toPrettyString());


        Query<Activation> query = activationRepository.filter(
                Activation_.imsi.equal("420050012800662").or(Activation_.imsi.equal("420050007389504")));

        Filter equal1 = Activation_.createdOn.equal(LocalDateTime.now());
        Filter equal2 = Activation_.terminationDate.equal(LocalDateTime.now());

        Filter or = equal1.or(equal2);
        // activationRepository.filter(Activation_.createdOn.equal(LocalDateTime.now()).or(fadfafdsfafsadfasdfdafsda))

        List<Activation> all = query.findAll();


        System.out.println("All:\n" + objectMapper.valueToTree(all).toPrettyString());

        //List<Tuple> allByColumn = filter.findAll(Activation_.msisdn.as("msisdn"));
        //Hibernate: select activation0_.MSISDN as col_0_0_, activation0_.IMSI as col_1_0_, esim1_.IMSI as col_2_0_ from activation activation0_ cross join esim esim1_ where activation0_.IMSI=? or activation0_.IMSI=?
        List<Tuple> allByColumn = query.findAll(Activation_.msisdn, Activation_.imsi, ESim_.imsi);

        System.out.println("Columns only:\n" + objectMapper.valueToTree(allByColumn).toPrettyString());

        //Hibernate: select activation0_.IMSI as col_0_0_, activation0_.MSISDN as col_1_0_ from activation activation0_ where activation0_.IMSI=? or activation0_.IMSI=?
        // List<Tuple> allByColumn1 = query.findAll(Activation_.imsi.as("myimsi"), Activation_.msisdn.as("mymsisdn"));
        List<Tuple> allByColumn1 = query.findAll(Activation_.imsi.as("myimsi"), Activation_.msisdn.as("mymsisdn"));
        //List<Tuple> allByColumn111 = query.findAll(Activation_.imsi,Activation_.id,Activation_.msisdn.as("mymsisdn"));
        System.out.println("Cols only by attrib alias:\n" + objectMapper.valueToTree(allByColumn1).toPrettyString());

        // {"columns":["lower(substring('dealerCode',1,3))"]}
        List<Tuple> allByColumn5 = query.findAll(lower(Activation_.dealerCode.substring(1, 3)), as(Activation_.id.sqrt(), "kvadraten"));
        //  List<Tuple> allByColumn5 = filter.findAll(lower(Activation_.dealerCode.),as(Activation_.id.sqrt(),"kvadraten"));
        List<Tuple> allByColumn55 = query.findAll(lower(Activation_.dealerCode.substring(1, 3)), Activation_.dealerCode.lower());
        List<Tuple> allByColumn51 = query.findAll(Activation_.dealerCode.substring(1, 3), Activation_.dealerCode.as("dealerCode"));
        List<Tuple> allByColumn52 = query.findAll(Activation_.dealerCode.concat(Activation_.dealerCode.lower()));
        List<Tuple> allByColumn53 = query.findAll(Activation_.id.max());
        List<Tuple> allByColumn54 = query.findAll(Activation_.id.max());
        List<Tuple> allByColumn56 = query.findAll(abs(Activation_.id.max()));
        //Selection<?> sel = "dealerCode".toLowerCase().substring(1, 3).literal().apply(null, null, null);
        System.out.println("Cols only by attrib & function in function:\n" + objectMapper.valueToTree(allByColumn5).toPrettyString());

        //Expression<String> expression=new LiteralExpression<String>("dealerCode".toLowerCase().substring(1,3));
        List<Tuple> allByColumn6 = query.findAll(literal("dealerCode".toLowerCase().substring(1, 3)));
        System.out.println("Cols only by attrib java+literal:\n" + objectMapper.valueToTree(allByColumn5).toPrettyString());


//        {
//            "filter":" imsiRangeStart<esim.imsi & imsiRangeEnd>esim.imsi & esim.statusID=3 & join(`subscriptionType`,`productCode=10 & brandId=1`,`LEFT`)",
//                "columns": ["as(min(esim.imsi),`minIMsi`)"],
//            "size": 1
//        }

        //Hibernate: select max(esim1_.IMSI) as col_0_0_ from subscriptiontypesimassociation subscripti0_ left outer join subscriptiontype subscripti3_ on subscripti0_.SubscriptionTypeId=subscripti3_.Id cross join esim esim1_ cross join subscriptiontype subscripti2_ where subscripti2_.BrandId=1 and subscripti2_.ProdcutCode=10 and esim1_.StatusID=3 and subscripti0_.ImsiRangeStart=esim1_.IMSI
        Query<SubscriptionTypeSimAssociation> query2 = subscriptionTypeSimAssociationRepository.filter(
                SubscriptionTypeSimAssociation_.imsiRangeStart.equal(ESim_.imsi.to(Long.class)).and(ESim_.statusID.equal(3).and(SubscriptionTypeSimAssociation_.subscriptionType.leftJoin(SubscriptionType_.productCode.equal("10").and(SubscriptionType_.brandId.equal(1))))));

        List<Tuple> allByColumn2 = query2.findAll(ESim_.id.max("minimsi"));
        System.out.println("Cols only by attrib function and alias:\n" + objectMapper.valueToTree(allByColumn2).toPrettyString());

        List<Tuple> allByColumn3 = query2.findAll(ESim_.id.max());
        System.out.println("Cols only by attrib & function (alias is autogenerated):\n" + objectMapper.valueToTree(allByColumn3).toPrettyString());

        //Hibernate: select activation0_.IMSI as col_0_0_, activation0_.ID as col_1_0_ from activation activation0_ where activation0_.IMSI in (? , ?)
        Query<Activation> query3 = activationRepository.filter(
                Activation_.imsi.in("420050012800662", "420050007389504"));

        List<Tuple> allByColumn7 = query3.findAll(Activation_.imsi, Activation_.id);
        System.out.println("Cols only by string & IN:\n" + objectMapper.valueToTree(allByColumn7).toPrettyString());

        //"filter": "(processInstanceId='processinstancelog.processInstanceId') & in('id',subfilter('variableinstancelog','','max(id)','processInstanceId,variableId'))
        //"columns":["variableId","value","processInstanceId","processId"],
        // Hibernate: select variablein0_.variableId as col_0_0_ from VariableInstanceLog variablein0_ cross join ProcessInstanceLog processins1_ where (variablein0_.id in (select max(variablein0_.id) from VariableInstanceLog variablein2_ group by variablein2_.processInstanceId , variablein2_.variableId)) and variablein0_.variableId=processins1_.processInstanceId
        Query<VariableInstanceLog> query4 = variableInstanceLogRepository.filter(
                VariableInstanceLog_.variableId.equal(ProcessInstanceLog_.processInstanceId.to(String.class)).and(
                        VariableInstanceLog_.id.in(variableInstanceLogRepository.filter().groupBy(VariableInstanceLog_.processInstanceId, VariableInstanceLog_.variableId).toSubQuery(VariableInstanceLog_.id.max())))
        );
        List<Tuple> allByColumn44 = query4.findAll(VariableInstanceLog_.variableId.as("variableId"));
        System.out.println("Cols only by attrib & IN SUBQUERY:\n" + objectMapper.valueToTree(allByColumn44).toPrettyString());


        // Call DB specific function
        // "datediff(date(activation.createdOn),'2017-06-25')"
        // Hibernate: select datediff(activation0_.CreatedOn, ?) as col_0_0_ from activation activation0_ inner join identitymaster identityma1_ on activation0_.IdentityMasterId=identityma1_.Id inner join subscriptiontype subscripti2_ on activation0_.SubscriptionTypeId=subscripti2_.Id where 1=1 and 1=1 and activation0_.CreatedOn<? limit ?
        List<Tuple> allByColumn46 = query5.findRange(0, 3,
                call("datediff", Integer.class, Activation_.createdOn, LocalDateTime.parse("2017-06-25T16:54:00"))
        );


        System.out.println("Cols by SQL function defined in Dialect extension:\n" + objectMapper.valueToTree(allByColumn46).toPrettyString());

        //List<Tuple> allByColumn4 = filter2.findAll(count(ESim_.id.max()));
        // System.out.println("Cols only by attrib & function in function:\n" + objectMapper.valueToTree(allByColumn4).toPrettyString());


        List<Tuple> allByColumn49 = query2.findAll(ESim_.statusID.mod(3));
        // List<Tuple> allByColumn49 = filter2.findAll(ESim_.imsi.mod(3));//with imsi shouldn't work cos imsi is string, required is Number
        System.out.println("Cols only by attrib & function in function:\n" + objectMapper.valueToTree(allByColumn49).toPrettyString());


        //BUG: Hibernate: select brand3_.Id as col_0_0_, brand3_.Name as col_1_0_ from activation activation0_ left outer join simreplacement simreplace2_ on activation0_.ID=simreplace2_.ActivationID inner join brand brand3_ on activation0_.BrandId=brand3_.Id cross join simreplacement simreplace1_ where 1=1 and simreplace1_.IMSI=? and activation0_.IMSI=?

        String imsi = "420050012800662";
        Filter findActivationORSimreplacmentByimsi = Activation_.imsi.equal(imsi).and(Activation_.simReplacements.leftJoin()).and(Activation_.brand.join()).and(SimReplacement_.imsi.equal(imsi));

        List<Tuple> allByColumn33 = activationRepository.filter(findActivationORSimreplacmentByimsi).findAll(Brand_.id, Brand_.name);

        System.out.println("Cols only by attrib & function in function:\n" + objectMapper.valueToTree(allByColumn33).toPrettyString());

//TODO:
        //Hibernate: select esim1_.IMSI as col_0_0_ from activation activation0_
        // inner join activationinitialorder activation2_ on activation0_.ID=activation2_.ActivationId
        // inner join activationinitialordertype activation3_ on activation2_.Id=activation3_.ActivationInitialOrderId
        // inner join identitymaster identityma4_ on activation0_.IdentityMasterId=identityma4_.Id cross join esim esim1_
        // where 1=1 and activation2_.TotalOrderAmountVATExcluded=12.0 and activation3_.ActivationOrderItemId=3
        // order by activation0_.MSISDN asc


//        List<Tuple> esimsi = activationRepository.filter(
//                Activation_.activationsInitialOrders.join
//                        ((ActivationInitialOrders_.activationInitialOrderTypes.join(
//                                ActivationInitialOrderType_.activationOrderItemId.equal(3))
//                                 .and(ActivationInitialOrders_.totalOrderAmountVATExcluded.equal(12.00))))
//                                        .and(Activation_.identityMaster.join()))
//                .sortBy(Sort.by(Activation_.MSISDN).ascending()).findAll(ESim_.imsi.as("esimsi"));
//        List<Tuple> esimsi1 = activationRepository.filter(
//                Activation_.imsi.equal("2323232323232323")).columns(ESim_.imsi.as("esimsi"))
//                .sortBy(Sort.by(Activation_.MSISDN).ascending()).findAllByColumn();
    }

    @Test
    public void specificationWithProjection() throws JsonProcessingException {

        activationRepository.findTop10ActivationByIdentityMaster_IdNumberAndActiveOrderByCreatedOnDesc("3433343443", true, ActivationEssentials.class);


        //List<Tuple> allTuples = activationRepository.findAllTuples(new String[]{"id","msisdn","active","imsi","dealerCode", "identitymaster.id", "identitymaster.idType.code"}, Activation_.id.greaterThan(0L).and(Activation_.identityMaster.join(IdentityMaster_.idType.join(IdentityMaster_.id.greaterThan(0L)))), Sort.unsorted());
        //Optional<Tuple> allTuples = activationRepository.findFirst(new SpecificationFunctionalInterface[]{ "identitymaster.id", "identitymaster.idType.code"}, Activation_.id.greaterThan(0L).and(Activation_.identityMaster.join(IdentityMaster_.idType.join(IdentityMaster_.id.greaterThan(0L)))), Sort.unsorted());


        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        //   module.addSerializer(Tuple.class, new TupleSerializer());
        module.addSerializer(Tuple.class, new TupleSerializer2());

        objectMapper.registerModule(module);

        SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();

        Page<ActivationEssentials> allPages = activationRepository.findAll(Activation_.id.greaterThan(0L).and(IdentityMaster_.id.greaterThan(0L)), ActivationEssentials.class, PageRequest.of(0, 10));

        List<Tuple> allTuples = activationRepository.findRange(Activation_.id.greaterThan(0L).and(IdentityMaster_.id.greaterThan(0L)), ActivationEssentials.class, 2, 5, Sort.by("msisdn"));

        Optional allTuplesFirst = activationRepository.findFirst(Activation_.id.greaterThan(0L).and(IdentityMaster_.id.greaterThan(0L)),ActivationEssentials.class,Sort.unsorted());

        Optional allPagesF = activationRepository.findFirst(Activation_.id.greaterThan(0L).and(IdentityMaster_.id.greaterThan(0L)), ActivationEssentials.class);


        //
        // JsonNode jsonNode = objectMapper.valueToTree(allTuples);
        JsonNode jsonNode;
        jsonNode = objectMapper.valueToTree(allTuples);

        System.out.println("Tuples:\n"+jsonNode.toPrettyString());


        jsonNode = objectMapper.valueToTree(allPages.getContent());

        System.out.println("Projection:\n"+jsonNode.toPrettyString());

        jsonNode = objectMapper.valueToTree(allPagesF.get());

        System.out.println("Tuples first:\n"+jsonNode.toPrettyString());

       // ReturnedType returnedType = ReturnedType.of(ActivationEssentials.class, Activation.class, projectionFactory);
        //final ResultProcessor resultProcessor = new ResultProcessor(projectionFactory, returnedType);
//returnedType.getInputPropertiesDescriptors().get(0).getReadMethod().getDeclaredAnnotations()
       // final List<ActivationEssentials> resultList = resultProcessor.processResult(allPages.getContent(), new TupleConverter(returnedType));

//        ActivationEssentials.IdentityMasterBasic identityMaster = resultList.get(0).getIdentityMaster();
//        String code1 = identityMaster.getIdType().getCode();
//        ActivationEssentials.IdentityMasterBasic.IdentityMasterIdType idType = resultList.get(0).getIdentityMaster().getIdType();
//

//        String code = resultList.get(0).getIdentityMaster().getIdType().getCode();
//
        // ActivationEssentials activationEssentials=projectionFactory.createProjection(ActivationEssentials.class,jsonNode);

        //ActivationEssentials activationEssentials = objectMapper.treeToValue(jsonNode, ActivationEssentials.class);

//

        //    // Create maps for each result tuple
//    List<ActivationEssentials> projectedResults = new ArrayList<>(allTuples.size());
//        for (Tuple tuple : allTuples) {
//        Map<String, Object> mappedResult = new HashMap<>(tuple.getElements().size());
//        for (TupleElement<?> element : tuple.getElements()) {
//            String name = element.getAlias();
//            mappedResult.put(name, tuple.get(name));
//        }
//
//
//        projectedResults.add(projectionFactory.createProjection(ActivationEssentials.class, mappedResult));
//    }


        //
        //factory.createProjection(ActivationEssentials.class,allTuples.get(0));

        //Page all = activationRepository.findAll(Activation_.id.greaterThan(0L).and(IdentityMaster_.id.greaterThan(0L)), ActivationEssentials.class,  PageRequest.of(0,10));

    }

    @Test
    public void specificationProjectionActivationSIMreplacement() throws JsonProcessingException {

        activationRepository.findTop10ActivationByIdentityMaster_IdNumberAndActiveOrderByCreatedOnDesc("3433343443", true, ActivationEssentials.class);
      //  simReplacementRepository.findSIMreplacementActivation("420050004439405", true, SimReplacementEssentials.class);

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Tuple.class, new TupleSerializer2());
        objectMapper.registerModule(module);

        SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();

  //      Page<ActivationEssentials> allPages = activationRepository.findAll(Activation_.id.greaterThan(0L).and(IdentityMaster_.id.greaterThan(0L)), ActivationEssentials.class, PageRequest.of(0, 10));

        Page<SimReplacementEssentials> pSimReplacementEssentials = simReplacementRepository.findAll(SimReplacement_.id.greaterThan(0L), SimReplacementEssentials.class, PageRequest.of(0, 2));

        List<Tuple> pSimReplacementEssentialsRange2 = simReplacementRepository.findRange(SimReplacement_.id.greaterThan(0L), SimReplacementEssentials.class,1,2, Sort.by("id"));


        Optional pSimReplacementFirst = simReplacementRepository.findFirst(SimReplacement_.id.greaterThan(0L), SimReplacementEssentials.class, Sort.by("id"));

  //      List<Tuple> allTuples = activationRepository.findRange(Activation_.id.greaterThan(0L).and(IdentityMaster_.id.greaterThan(0L)), ActivationEssentials.class, 2, 5, Sort.by("msisdn"));

  //      Optional allTuplesFirst = activationRepository.findFirst(Activation_.id.greaterThan(0L).and(IdentityMaster_.id.greaterThan(0L)),ActivationEssentials.class,Sort.unsorted());

   //     Optional allPagesF = activationRepository.findFirst(Activation_.id.greaterThan(0L).and(IdentityMaster_.id.greaterThan(0L)), ActivationEssentials.class);


        JsonNode jsonNode;
        //List<Tuple> tupleList = null;
        jsonNode = objectMapper.valueToTree(pSimReplacementEssentials.getContent().toArray());
        System.out.println("SimReplacement:\n"+jsonNode.toPrettyString());

        jsonNode = objectMapper.valueToTree(pSimReplacementEssentialsRange2);
        System.out.println("SimReplacement range tuple:\n"+jsonNode.toPrettyString());

        jsonNode = objectMapper.valueToTree(pSimReplacementFirst.get());
        System.out.println("SimReplacement First:\n"+jsonNode.toPrettyString());

//        jsonNode = objectMapper.valueToTree(allTuples);
//        System.out.println("Tuples:\n"+jsonNode.toPrettyString());
//
//
//        jsonNode = objectMapper.valueToTree(allPages.getContent());
//        System.out.println("Projection:\n"+jsonNode.toPrettyString());
//
//        jsonNode = objectMapper.valueToTree(allPagesF.get());
//        System.out.println("Tuples first:\n"+jsonNode.toPrettyString());

    }

    @Test
    public void findActivationORSimreplacmentByimsi() {

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Tuple.class, new TupleSerializer2());
        objectMapper.registerModule(module);
//420050004567941 local
        //420050003037035 predprod
        //420050007389555 local simR
        String imsiT="420050007389555";
        String brandName="";

        Filter findActivationByimsi = Brand_.activations.join().and(Activation_.imsi.equal(imsiT));
        Optional<Tuple> brandByImsi = brandRepository.filter(findActivationByimsi).findFirst(Brand_.name.as(Brand_.NAME));
        brandName = brandByImsi.map(tuple -> ((String) tuple.get(Brand_.NAME))).orElse(null);
        if (brandName == null) {
            //Filter findsimReplacementByimsi = SimReplacement_.activation.join().and(Brand_.activations.join()).and(SimReplacement_.imsi.equal(imsiT));
            //IdentityMaster_.activations.join(Activation_.terminationDate.isNull().and(Activation_.simReplacements.join(SimReplacement_.imsi.equal(imsiT))))

         //   Filter findsimReplacementByimsi = SimReplacement_.activation.join(SimReplacement_.imsi.equal(imsiT).and(Activation_.brand.join()));

         //   Filter findsimReplacementByimsi = SimReplacement_.activation.join(Activation_.brand.join().and(SimReplacement_.imsi.equal(imsiT)));

            Filter findsimReplacementByimsi = Brand_.activations.join(Activation_.simReplacements.join(SimReplacement_.imsi.equal(imsiT)));

            //Filter findsimReplacementByimsi = Brand_.activations.join().and(Activation_.simReplacements.join().and(SimReplacement_.imsi.equal(imsiT)));
            Optional<Tuple> brandsimReplacementByImsi = brandRepository.filter(findsimReplacementByimsi).findFirst(Brand_.name.as(Brand_.NAME));
            brandName = brandsimReplacementByImsi.map(tuple -> ((String) tuple.get(Brand_.NAME))).orElse(null);
        }


        Activation activation = this.activationRepository.findByImsi(imsiT, Activation.class);
        if (activation == null) {
            SimReplacement simReplacement = simReplacementRepository.findByImsi(imsiT).orElse(null);
            if (simReplacement != null && simReplacement.getActivation() != null) {
                activation = simReplacement.getActivation();
            }
        }
//        select B.Name
//        from activation A
//        inner join brand B on B.Id=A.BrandId
//        where A.IMSI = '420050003037035'

//        union ALL
//        select B.Name
//        from simreplacement S
//        inner join activation A on A.ID=S.ActivationID
//        inner join brand B on B.Id=A.BrandId
//        where S.IMSI = '420050003037035'

//        Filter findActivationORSimreplacmentByimsi = Brand_.activations.join().and(Activation_.imsi.equal(imsiT)).or(Brand_.activations.join(Activation_.simReplacements.leftJoin().and(SimReplacement_.imsi.equal(imsiT))));
//        Optional<Tuple> brand = brandRepository.filter(findActivationORSimreplacmentByimsi).findFirst(Brand_.name.as(Brand_.NAME));
//        return brand.map(tuple -> ((String) tuple.get(Brand_.NAME))).orElse(null);






        Integer activationOrderTypeId=3;
        String dealerCode="Dir-11277";
        LocalDateTime start = LocalDateTime.of(2023,5,1, 0,0);;
        LocalDateTime end = LocalDateTime.of(2023,5,24, 0,0);
        Integer page = 1;
        Integer pageSize = 100;

        Filter filter3 = ActivationInitialOrders_.activationInitialOrderTypes.join().and(ActivationInitialOrderType_.activationOrderItemId.equal(activationOrderTypeId)).and(ActivationInitialOrders_.activation.join().and(Activation_.dealerCode.equal(dealerCode)).and(Activation_.createdOn.greaterThanOrEqualTo(start)).and(Activation_.createdOn.lessThanOrEqualTo(end)));
        Query<ActivationInitialOrders> activationInitialOrdersQuery = activationInitialOrdersRepository.filter(filter3).groupBy(substring(Activation_.createdOn.to(String.class), 1, 10));

//        SpecificationFunctionalInterface[] cols = new SpecificationFunctionalInterface[]{ActivationInitialOrders_.totalOrderAmountVATIncluded.sum(), ActivationInitialOrders_.totalOrderAmountVATExcluded.sum(), Activation_.id.count(), Activation_.createdOn.greatest()};
//        activationInitialOrdersQuery.findAll(PageRequest.of(page, pageSize, Sort.unsorted()), cols).getContent();



        //activationInitialOrdersQuery.findAll(ActivationInitialOrders_.totalOrderAmountVATIncluded.sum(), ActivationInitialOrders_.totalOrderAmountVATExcluded.sum(), Activation_.id.count(), Activation_.id.max(), Activation_.createdOn.as(Activation_.CREATED_ON));



        String imsi = "420050000005393";

//Activation_.imsi.equal(imsi).and(
        Filter findActivationORSimreplacmentByimsi = Activation_.simReplacements.leftJoin(SimReplacement_.imsi.equal(imsi));
        List<Tuple> allByColumn36 = activationRepository.filter(findActivationORSimreplacmentByimsi).findAll(Brand_.id, Brand_.name);
        System.out.println("Joins with condition:\n" + objectMapper.valueToTree(allByColumn36).toPrettyString());

//        Filter filter = ActivationInitialOrders_.activationInitialOrderTypes.join().and(ActivationInitialOrderType_.activationOrderItemId.equal(activationOrderTypeId)).and(ActivationInitialOrders_.activation.join().and(Activation_.dealerCode.equal(dealerCode)).and(Activation_.createdOn.greaterThanOrEqualTo(start)).and(Activation_.createdOn.lessThanOrEqualTo(end)));
//
//        Optional<Tuple> brand = brandRepository.filter(filter).findFirst(Brand_.name.as(Brand_.NAME));

        //Brand_.activations.join(Activation_.imsi.equal(imsi)).or(
        Filter filter = Brand_.activations.join(Activation_.simReplacements.leftJoin(SimReplacement_.imsi.equal(imsi)));
        Optional<Tuple> brand = brandRepository.filter(filter).findFirst(Brand_.name.as(Brand_.NAME));
        System.out.println("Joins with condition:\n" + objectMapper.valueToTree(brand).toPrettyString());
        brand.map(tuple -> ((String) tuple.get(Brand_.NAME))).orElse(null);

    }

        @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Unit {
        String value();
    }

    @Test
    public void TestContextual() throws JsonProcessingException {


// Create own annotation storing your unit value


// Create custom serializer checking @Unit annotation

        class UnitSerializer extends StdSerializer<Integer> implements ContextualSerializer {

            private String unit;

            public UnitSerializer() {
                super(Integer.class);
            }

            public UnitSerializer(String unit) {
                super(Integer.class);
                this.unit = unit;
            }

            @Override
            public void serialize(Integer value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeString(String.format("%d %s", value, unit));
            }

            @Override
            public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
                String unit = null;
                Unit ann = null;
                if (property != null) {
                    ann = property.getAnnotation(Unit.class);
                }
                if (ann != null) {
                    unit = ann.value();
                }
                return new UnitSerializer(unit);
            }
        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        class X {
            @JsonSerialize(using = UnitSerializer.class)
            @Unit("mm")
            private int length;
        }


        X x = new X(10);
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(x));

    }
}



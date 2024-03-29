package com.many.miniproject1.main;

import com.many.miniproject1.apply.ApplyRepository;
import com.many.miniproject1.apply.ApplyRequest;
import com.many.miniproject1.offer.OfferRepository;
import com.many.miniproject1.offer.OfferRequest;
import com.many.miniproject1.post.Post;
import com.many.miniproject1.post.PostRepository;
import com.many.miniproject1.post.PostResponse;
import com.many.miniproject1.resume.Resume;
import com.many.miniproject1.resume.ResumeRepository;
import com.many.miniproject1.resume.ResumeResponse;
import com.many.miniproject1.scrap.ScrapRepository;
import com.many.miniproject1.scrap.ScrapRequest;
import com.many.miniproject1.skill.SkillRepository;
import com.many.miniproject1.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class MainController {
    private final MainRepository mainRepository;
    private final ResumeRepository resumeRepository;
    private final PostRepository postRepository;
    private final SkillRepository skillRepository;
    private final HttpSession session;
    private final OfferRepository offerRepository;
    private final ApplyRepository applyRepository;
    private final ScrapRepository scrapRepository;
    private Integer postChoose;

    //메인 구직 공고
    @GetMapping("/company/main")
    public String resumeForm(HttpServletRequest request) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        List<ResumeResponse.DetailDTO> resumeList = mainRepository.findAllResume();
        request.setAttribute("resumeList", resumeList);
        System.out.println(resumeList.size());

        ArrayList<ResumeResponse.DetailSkillDTO> resumeSkillList = new ArrayList<>();
        for (int i = 0; i < resumeList.size(); i++) {
            List<String> skills = skillRepository.findByResumeId(resumeList.get(i).getId());
            System.out.println(skills);
            ResumeResponse.DetailDTO resume = resumeList.get(i);
            System.out.println(resume);

            resumeSkillList.add(new ResumeResponse.DetailSkillDTO(resume, skills));
            System.out.println(resumeSkillList.get(i));
        }
        Boolean isCompany = false;
        //기업인지 개인인지 구분
        if (sessionUser != null) {
            String role = sessionUser.getRole();
            System.out.println(role);

            if (role.equals("company")) {
                isCompany = true;
            }
        }
        request.setAttribute("isMatchingCompany", isCompany);
        request.setAttribute("resumeSkillList", resumeSkillList);
        request.setAttribute("sessionuser", sessionUser);


        return "company/main";
    }


    @GetMapping("/resume/detail/{id}")
    public String resumeDetailForm(@PathVariable int id, HttpServletRequest request) {
        System.out.println("id: " + id);
        //내용작성
        // DTO에 ArrayList는 초기화 해두어도 된다.
        List<String> skills = skillRepository.findByResumeId(id);
        ResumeResponse.DetailSkillDTO detailDTO=new ResumeResponse.DetailSkillDTO(mainRepository.findMainResume(id),skills);
        System.out.println(detailDTO);

        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser != null) {
            //이력서 선택
            System.out.println(sessionUser);
            Integer companyId = sessionUser.getId();
            System.out.println(companyId);
            List<Post> postList = mainRepository.findPost(companyId);
            System.out.println(postList);

            //기업인지 개인인지 구분
            String role = sessionUser.getRole();
            System.out.println(role);
            Boolean isCompany = false;
            if (role.equals("company")) {
                isCompany = true;
            }
            System.out.println(isCompany);
            request.setAttribute("isMatchingCompany", isCompany);
            System.out.println(isCompany);
            request.setAttribute("postList", postList);
        }
        request.setAttribute("sessionuser", sessionUser);
        request.setAttribute("resume", detailDTO);
        return "company/resumeDetail";
    }

    @PostMapping("/resume/detail/{id}/offer")
    public String companyResumeOffer(@PathVariable int id, @RequestParam("postChoice") Integer postChoice) {
        System.out.println("======================" + postChoice);
        User sessionUser = (User) session.getAttribute("sessionUser");
        System.out.println(sessionUser);
        Integer companyId = sessionUser.getId();
        System.out.println(companyId);

        OfferRequest.SaveDTO saveDTO = new OfferRequest.SaveDTO();
        saveDTO.setResumeId(id);
        saveDTO.setPostId(postChoice);
        saveDTO.setCompanyId(companyId);
        saveDTO.setPersonId(mainRepository.findPersonId(id));


        System.out.println(saveDTO);
        offerRepository.save(saveDTO);

        return "redirect:/resume/detail/{id}";
    }

    @PostMapping("/resume/detail/{id}/scrap")
    public String companyResumeScrap(@PathVariable int id) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        System.out.println(sessionUser);
        Integer companyId = sessionUser.getId();
        System.out.println(companyId);

        ScrapRequest.SaveResumeDTO saveDTO = new ScrapRequest.SaveResumeDTO();
        saveDTO.setResumeId(id);
        saveDTO.setCompanyId(companyId);

        System.out.println(saveDTO);
        scrapRepository.saveResume(saveDTO);

        return "redirect:/resume/detail/{id}";
    }

    //메인 채용 공고
    @GetMapping({"/person/main", "/", "/index"})
    public String postForm(HttpServletRequest request) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        List<Post> postList = postRepository.findAll();
        System.out.println(postList.size());


        ArrayList<MainResponse.postDTO> postSkillList = new ArrayList<>();
        for (int i = 0; i < postList.size(); i++) {
            List<String> skills = skillRepository.findByPostId(postList.get(i).getId());
            System.out.println(skills);
            Post post = (Post) postList.get(i);
            System.out.println(post);
            postSkillList.add(new MainResponse.postDTO(post, skills));
            System.out.println(postSkillList.get(i));
        }

        Boolean isCompany = false;
        //기업인지 개인인지 구분
        if (sessionUser != null) {
            String role = sessionUser.getRole();
            System.out.println(role);

            if (role.equals("company")) {
                isCompany = true;
            }
        }
        request.setAttribute("isMatchingCompany", isCompany);
        request.setAttribute("sessionuser", sessionUser);
        request.setAttribute("postSkillList", postSkillList);

        return "person/main";
    }


    @GetMapping("/post/detail/{id}")
    public String postDetailForm(@PathVariable int id, HttpServletRequest request) {
        System.out.println("id: " + id);

        //내용작성
        PostResponse.DetailDTO detailDTO = postRepository.findById(id);
        List<String> skills = skillRepository.findByResumeId(id);

        detailDTO.setSkill(skills);


        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser != null) {
            //이력서 선택
            System.out.println(sessionUser);
            Integer personId = sessionUser.getId();
            System.out.println(personId);
            List<Resume> resumeList = mainRepository.findResume(personId);
            System.out.println(resumeList);
            //기업인지 개인인지 구분
            String role = sessionUser.getRole();
            System.out.println(role);
            Boolean isCompany = false;
            if (role.equals("company")) {
                isCompany = true;
            }
            request.setAttribute("isMatchingCompany", isCompany);
            request.setAttribute("resumeList", resumeList);
        }
        request.setAttribute("sessionuser", sessionUser);

        request.setAttribute("post", detailDTO);
        return "person/postDetail";
    }

    @PostMapping("/post/detail/{id}/apply")
    public String personPostApply(@PathVariable int id, @RequestParam("resumeChoice") Integer resumeChoice) {
        System.out.println("======================" + resumeChoice);
        User sessionUser = (User) session.getAttribute("sessionUser");
        System.out.println(sessionUser);
        Integer personId = sessionUser.getId();
        System.out.println(personId);

        ApplyRequest.SaveDTO saveDTO = new ApplyRequest.SaveDTO();
        saveDTO.setResumeId(resumeChoice);
        saveDTO.setPostId(id);
        saveDTO.setCompanyId(mainRepository.findCompanyId(id));

        saveDTO.setPersonId(personId);
        saveDTO.setIsPass("검토중");


        System.out.println("save : " + saveDTO);
        applyRepository.save(saveDTO);

        return "redirect:/post/detail/{id}";
    }

    @PostMapping("/post/detail/{id}/scrap")
    public String personPostScrap(@PathVariable int id) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        System.out.println(sessionUser);
        Integer personId = sessionUser.getId();
        System.out.println(personId);

        ScrapRequest.SavePostDTO saveDTO = new ScrapRequest.SavePostDTO();
        saveDTO.setPostId(id);
        saveDTO.setPersonId(personId);

        System.out.println(saveDTO);
        scrapRepository.savePost(saveDTO);
        return "redirect:/post/detail/{id}";
    }


    //맞춤 공고 - 기업이 보는 매칭 이력서
    @GetMapping("/company/matching")
    public String matchingResumeForm(HttpServletRequest request) {


        //Boolean isCompany
        return "person/matching";
    }

//    @GetMapping("/matching/resume/detail/{id}")
//    public String matchingResumeDetailForm(@PathVariable int id, HttpServletRequest request) {
//        System.out.println("id: " + id);
//
//        ResumeResponse.DetailDTO detailDTO = resumeRepository.findById(id);
//        List<String> skills = skillRepository.findByResumeId(id);
//
//        //detailDTO.setSkill(skills);
//
//        request.setAttribute("resume", detailDTO);
//        return "person/resumeDetail";
//    }
//
//    @PostMapping("/matching/resume/detail/{id}/offer")
//    public String matchingCompanyResumeOffer() {
//
//        return "redirect:/matching/resume/detail/{id}";
//    }
//
//    @PostMapping("/matching/resume/detail/{id}/scrap")
//    public String matchingCompanyResumeScrap() {
//        return "redirect:/matching/resume/detail/{id}";
//    }

    //맞춤 공고 - 개인이 보는 매칭 공고
    @GetMapping("/person/matchingform")
    public String matchingPostForm(HttpServletRequest request,MainRequest.postIdDTO requestDTO) {
        //공고 가져오기
        User sessionUser = (User) session.getAttribute("sessionUser");
        System.out.println(sessionUser);
        Integer userId = sessionUser.getId();
        List<Post> postList = mainRepository.findPost(userId);
        request.setAttribute("postList", postList);



        //매칭할 공고 가져오기
        int postChoose=requestDTO.getPostId();

        //매칭할 공고 스킬 가져와 리스트에 담기
        List<String> postSkill=skillRepository.findByPostId(postChoose);
        //전체 이력서 새로운 이력서점수리스트에 담기, 점수는 0으로 시작
        List<MainResponse.ResumeSkillDTO> resumeSkillScore=new ArrayList<>();
        for (int i=0; i<skillRepository.findAll().size();i++){
            int resumeId=skillRepository.findAll().get(i).getResumeId();
            resumeSkillScore.add(new MainResponse.ResumeSkillDTO(resumeId, 0));
        }
        //공고스킬만큼 반복문 돌리기
        for(int i=0; i<postSkill.size();i++){
            //모든 스킬테이블에서 비교하기위해 반복문 돌리기
            for(int j=0; j<skillRepository.findAll().size();j++){
                //스킬테이블과 공고스킬 비교하기
                if(postSkill.get(i)==skillRepository.findAll().get(j).getSkill()){
                    //스킬테이블에서 같은 스킬 찾아서 거기 이력서아이디 가져오기
                    int resumeId=skillRepository.findAll().get(j).getId();
                    //이력서점수리스트 만큼 반복문 돌리기
                    for (int k = 0; k < resumeSkillScore.size(); k++) {
                        //이력서점수리스트의 이력서아이디와 스킬테이블 이력서 아이디와 같으면 이력서 점수리스트에 해당하는 점수 1점 올리기
                        if(resumeSkillScore.get(k).getResumeId()==resumeId){
                            resumeSkillScore.get(k).setScore(resumeSkillScore.get(k).getScore() + 1);
                        }
                    }
                }
            }
        }
        //2점이상 이력서아이다만 가져와 리스트 만들기
        List<Integer>finalResumeSkillScore=new ArrayList<>();
        for (int i=0; i<resumeSkillScore.size();i++){
            if(resumeSkillScore.get(i).getScore()>1){
                int two=resumeSkillScore.get(i).getResumeId();
                finalResumeSkillScore.add(two);
            }
        }

        List<ResumeResponse.DetailDTO> resumeList =new ArrayList<>();

        for (int i=0; i<finalResumeSkillScore.size();i++){
            int resumeId=finalResumeSkillScore.get(i);
            resumeList.add(mainRepository.findMainResume(resumeId));
        }

        request.setAttribute("resumeList", resumeList);
        System.out.println(resumeList.size());

        ArrayList<ResumeResponse.DetailSkillDTO> resumeSkillList = new ArrayList<>();
        for (int i = 0; i < resumeList.size(); i++) {
            List<String> skills = skillRepository.findByResumeId(resumeList.get(i).getId());
            System.out.println(skills);
            ResumeResponse.DetailDTO resume = resumeList.get(i);
            System.out.println(resume);

            resumeSkillList.add(new ResumeResponse.DetailSkillDTO(resume, skills));
            System.out.println(resumeSkillList.get(i));
        }



        return "company/matching";
    }

    @PostMapping("/person/matching")
    public String matchingPost(@RequestParam("postChoice") Integer postChoice) {
        postChoose=postChoice;
        return  "redirect:/person/matchingform";
    }
//
//    @GetMapping("/matching/post/detail/{id}")
//    public String matchingPostDetailForm(@PathVariable int id, HttpServletRequest request) {
//
//        PostResponse.DetailDTO detailDTO = postRepository.findById(id);
//        List<String> skills = skillRepository.findByResumeId(id);
//
//        detailDTO.setSkill(skills);
//
//        request.setAttribute("post", detailDTO);
//        return "company/postDetail";
//    }
}




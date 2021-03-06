package com.spring.madi;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for the application home page.
 */

@Controller
public class FrontController {

	@Autowired
	private RecipeDAOService recipeDAOService;
	@Autowired
	private BoardDAOService boardDAOService;
	@Autowired
	private MessageDAOService messageDAOService;
	@Autowired
	private NotificationDAOService notificationDAOService;
	@Autowired
	private MemberDAOService memberDAOService;
	@Autowired
	private JavaMailSender mailSender;

	private static final Logger logger = LoggerFactory.getLogger(FrontController.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		return "index";
	}

	// 게시물 보기 controller
	@RequestMapping("/postList.do")
	public ModelAndView postList(RecipeVO recipeVO, HttpSession session) {
		// 모델 & 뷰 연결 객체 생성
		ModelAndView result = new ModelAndView();
		// 레시피 리스트 조회
		List<RecipeVO> postList = recipeDAOService.postList();

		// Header 기본 정보(알림/메시지/유저이미지) 정보 받아오기
		String user_id = (String) session.getAttribute("user_id");
		// 내 정보 받아오기
		MemberVO memberVO = memberDAOService.getUserInfoById(user_id);
		// message 리스트 받아오기
		ArrayList<MessageVO> messageList = messageDAOService.getMyMessageById(user_id);
		// 알림 리스트 받아오기
		ArrayList<NotificationVO> notificationList = notificationDAOService.getMyNoticeById(user_id);
		// Header 내 냉장고 재료 받아오기
		ArrayList<MemberBoxVO> myIrdntList = memberDAOService.getMyIrdntByUserId(user_id);
		// 모델에 객체 추가
		result.addObject("memberVO", memberVO);
		result.addObject("messageList", messageList);
		result.addObject("notificationList", notificationList);
		result.addObject("postList", postList);
		result.addObject("myIrdntList", myIrdntList);
		// 연결 뷰 지정
		result.setViewName("postList");
		return result;
	}

	// 성빈 : 로그인 모델
	// 로그인 성공 시 session에 기본 정보를 저장하고 recipe.do 페이지로 이동한다
	// 로그인 실패 시 실패한 이유를 알려주고 index화면에 머무른다
	// 로그인 메소드 비정상 수행 수정 필요
	@RequestMapping("/login.do")
	public String loginMadi(MemberVO memberVO, HttpServletResponse response, HttpSession session) {
		// 입력된 아이디로부터 DB에 있는 password 조회
		MemberVO password = memberDAOService.getPasswordByUserId(memberVO);
		// DB의 password가 존재하는지는지 검사
		if (password.getUser_pw() == null) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html");
			try {
				PrintWriter writer = response.getWriter();
				writer.write("<script>alert('아이디가 존재하지 않습니다.\n아이디를 다시 확인해주세요.')</script>");
				System.out.println("system  :  로그인 화면, 로그인 에러 발생(아이디 체크). 로그인 실패로 인한 실패 알림 메시지 전송 실패sd");
			} catch (Exception e) {
				System.out.println("system  :  로그인 화면, 로그인 에러 발생(아이디 체크). 로그인 실패로 인한 실패 알림 메시지 전송 실패");
				e.printStackTrace();
				return "redirect:/";
			}
			return "redirect:/";
		} else {
			if (!(password.getUser_pw().equals(memberVO.getUser_pw()))) {
				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html");
				try {
					PrintWriter writer = response.getWriter();
					writer.write("<script>alert('비밀번호가 일치하지 않습니다.\n비밀번호를 다시 확인해주세요.')</script>");
					System.out.println("system  :  로그인 화면, 로그인 에러 발생(아이디 체크). 로그인 실패로 인한 실패 알림 메시지 전송 실패as");
				} catch (Exception e) {
					System.out.println("system  :  로그인 화면, 로그인 에러 발생(비밀번호 체크). 로그인 실패로 인한 실패 알림 메시지 전송 실패");
					e.printStackTrace();
					return "redirect:/";
				}
				return "redirect:/";
			} else {
				MemberVO userInfo = memberDAOService.getUserInfoById(memberVO.getUser_id());
				session.setAttribute("user_id", userInfo.getUser_id());
				session.setAttribute("user_email", userInfo.getUser_email());
				session.setAttribute("user_name", userInfo.getUser_name());
				session.setAttribute("user_img", userInfo.getUser_img());
				return "redirect:/recipe.do";
			}
		}
	}

	// 성빈 : 로그아웃 모델
	// 로그아웃 시 session에 저장한 모든 객체들을 드롭한다.
	// 드롭 메소드는 invalidate을 사용
	@RequestMapping("/logout.do")
	public String logoutMadi(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}

	// 성빈 : 회원가입 모델
	// DB에 Insert한 뒤 초기화면으로 돌아간다.
	@RequestMapping("/join.do")
	public String joinMadi(MemberVO memberVO, HttpServletResponse response) {
		memberVO.setUser_img("");
		memberDAOService.setMember(memberVO);
		return "redirect:/";
	}
	
	@RequestMapping(value="/join.do", params="ck=id")
	@ResponseBody
	public String checkUserId(String user_id) {
		return Integer.toString(memberDAOService.checkUserId(user_id));
	}
	@RequestMapping(value="/join.do", params="ck=email")
	@ResponseBody
	public String checkUserEmail(MemberVO memberVO) {
		return Integer.toString(memberDAOService.checkEmail(memberVO));
	}
	
	// 카카오톡 처음 로그인
	@RequestMapping("/sns_join.do")
	public String snsJoin(MemberVO memberVO,  Model model, HttpSession session, HttpServletResponse response ) {
		
		//ModelAndView model = new ModelAndView(); 
		System.out.println(memberVO.getUser_id());
		
		//이메일 중복체크 확인입니다.
		int x = memberDAOService.checkEmail(memberVO);
		System.out.println("x : " + x);
		if (x != 0) {
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter writer;
			try {
				writer = response.getWriter();
				writer.println("<script>");
				writer.println("alert('중복된 이메일입니다. 다른 계정을 확인해 보세요.');");
				writer.println("</script>");
				
				model.addAttribute("x", x);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return "sns_join";
		} else{
		
		System.out.println(memberVO.getUser_id());
		//디비에 저장
		memberDAOService.set_Member(memberVO);
		
		
		//아이디로 DB에서 select 합니다
		MemberVO userInfo = memberDAOService.getUserInfoById(memberVO.getUser_id());
		// Header에 들어가야 할 기본 데이터 읽어오기
		// 내 알림 메시지 읽어오기
		ArrayList<NotificationVO> notificationList = notificationDAOService.getMyNoticeById(userInfo.getUser_id());
		// 내 개인 메시지 읽어오기
		ArrayList<MessageVO> messageList = messageDAOService.getMyMessageById(userInfo.getUser_id());
		// 내 개인정보 읽어오기
		MemberVO memVO= memberDAOService.getMember(userInfo.getUser_id());
		// 내 냉장고 재료 목록 불러오기
		ArrayList<MemberBoxVO> myIrdntList = memberDAOService.getMyIrdntByUserId(userInfo.getUser_id());
		
		
		//헤더정보 모델에 저장
		model.addAttribute("messageList", messageList);
		model.addAttribute("notificationList", notificationList);
		model.addAttribute("MemberVO", memVO);
				
		//아이디정보 세션 저장
		//세션으로 저장해서 전달해 주기 session.getAttribute 로 값 받을수 있어요
		session.setAttribute("user_id", userInfo.getUser_id());
		session.setAttribute("user_email", userInfo.getUser_email());
		session.setAttribute("user_name", userInfo.getUser_name());
		session.setAttribute("user_img", userInfo.getUser_img());
		
		
		}
		return "redirect:/recipe.do";
		
	}
	



	// (진산)mypage.do 작업
	@RequestMapping("/mypage.do")
	public ModelAndView myPage(HttpServletRequest request, HttpSession session) {
		// ModelAndView 객체 생성
		ModelAndView result = new ModelAndView();
		// Session으로부터 사용자 아이디 읽어오기
		String user_id = (String) session.getAttribute("user_id");
		if (user_id == null) {
			System.out.println("SYSTEM  :  Controller, myPage.do; 세션 아이디 삭제 또는 비정상 접근에 의한 로그아웃 또는 접근 불가");
			result.setViewName("redirect:/");
			return result;
		}
		MemberFollowVO memberFollowVO = new MemberFollowVO();
		memberFollowVO.setUser_id(user_id);
		memberFollowVO.setFollowing_user_id(user_id);
		// Header에 들어가야 할 기본 데이터 읽어오기
		// 내 알림 메시지 읽어오기
		ArrayList<NotificationVO> notificationList = notificationDAOService.getMyNoticeById(user_id);
		// 내 개인 메시지 읽어오기
		ArrayList<MessageVO> messageList = messageDAOService.getMyMessageById(user_id);
		// 내 개인정보 읽어오기
		MemberVO memberVO = memberDAOService.getMember(user_id);
		// 내 냉장고 재료 목록 불러오기
		ArrayList<MemberBoxVO> myIrdntList = memberDAOService.getMyIrdntByUserId(user_id);
		
		// Mypage에서 쓰일 기본 정보 읽어오기
		// 팔로워 리스트
		List<MemberVO> followerList = memberDAOService.getFollower(user_id);
		// 팔로잉 리스트
		List<MemberVO> followingList = memberDAOService.getFollowing(user_id);
		// 팔로워 추천 리스트
		List<MemberVO> followers = memberDAOService.getRecommendFollower(user_id);
		List<MemberVO> followerCnt = memberDAOService.getRecommendByFollowerCnt(user_id);
		List<MemberVO> likeCnt = memberDAOService.getRecommendByLikeCnt();
		List<MemberVO> recipeCnt = memberDAOService.getRecommendByRecipeCnt();
		Map<String, List<MemberVO>> recommendMap = new HashMap<String, List<MemberVO>>();
		recommendMap.put("followers", followers);
		recommendMap.put("followerCnt", followerCnt);
		recommendMap.put("likeCnt", likeCnt);
		recommendMap.put("recipeCnt", recipeCnt);
		// 타임라인 구하기
		ArrayList<BoardVO> timeLine = boardDAOService.getTimelineBoard(memberFollowVO);
		// 내 게시글 리스트..카운트에도 쓴다
		List<BoardVO> myBoardList = boardDAOService.getBoards(user_id);
		// 나한테 온 팔로우 신청
		ArrayList<NotificationVO> followRequest = notificationDAOService.getFollowRequest(user_id);
		// 내 게시글의 시간 구하기(연월일)
		// String time= boardDAOService.getTime(user_id);
		// Object 추가
		result.addObject("notificationList", notificationList);
		result.addObject("messageList", messageList);
		result.addObject("memberVO", memberVO);
		result.addObject("myIrdntList", myIrdntList);
		result.addObject("followerList", followerList);
		result.addObject("followingList", followingList);
		result.addObject("recommendMap", recommendMap);
		result.addObject("myBoardList", myBoardList);
		result.addObject("timeLine", timeLine);
		result.addObject("followRequest", followRequest);
		
		result.setViewName("mypage_v2");
		return result;
	}

	// (진산) 타임라인 팔로잉+본인 글 보기
	@RequestMapping("/allBoard.do")
	public ModelAndView getAllBoard(BoardVO boardVO, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		MemberVO memberVO = memberDAOService.getUserInfoById(user_id);
		ModelAndView result = new ModelAndView();
		// 모든 게시글 위해서 불러옴
		List<BoardVO> allBoardList = boardDAOService.getAllBoards(user_id);
		System.out.println("user" + user_id);
		System.out.println("size " + allBoardList.size());
		result.addObject("allBoardList", allBoardList);
		result.addObject("memberVO", memberVO);
		result.setViewName("allBoard");
		return result;
	}

	// (진산) 내 게시글만 보기
	@RequestMapping("/myBoard.do")
	public ModelAndView getMyBoard(BoardVO boardVO, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		MemberVO memberVO = memberDAOService.getUserInfoById(user_id);
		ModelAndView result = new ModelAndView();
		List<BoardVO> myBoardList = boardDAOService.getBoards(user_id);
		System.out.println("user" + user_id);
		System.out.println("size " + myBoardList.size());
		result.addObject("myBoardList", myBoardList);
		result.addObject("memberVO", memberVO);
		result.setViewName("myBoard");
		return result;
	}

	// (진산) 내 사진들 보기
	@RequestMapping("/myPhoto.do")
	public ModelAndView getMyPhoto(BoardVO boardVO, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		ModelAndView result = new ModelAndView();
		List<BoardVO> myBoardList = boardDAOService.getBoards(user_id);
		System.out.println("user" + user_id);
		System.out.println("size " + myBoardList.size());
		result.addObject("myBoardList", myBoardList);
		result.setViewName("myPhoto");
		return result;
	}

	// (진산)팔로잉 한 명 삭제
	@RequestMapping("/deleteFollowing.do")
	@ResponseBody
	public String deleteFollowing(String user_id, String following_user_id) {

		// 팔로잉 삭제
		memberDAOService.deleteFollowing(user_id, following_user_id);

		// 팔로우 취소 알림 메시지 보내기
		NotificationVO notificationVO = new NotificationVO();
		notificationVO.setNotice_to(following_user_id);
		notificationVO.setNotice_from(user_id);
		notificationVO.setNotice_type("팔로우 거부");
		notificationVO.setContent(user_id + "님께서 팔로잉를 취소하셨습니다.");
		notificationDAOService.sendNoticeById(notificationVO);

		List<MemberVO> followingList = memberDAOService.getFollowing(user_id);
		
		return Integer.toString(followingList.size());
	}

	// (진산)팔로워 한 명 삭제
	@RequestMapping("/deleteFollower.do")
	@ResponseBody
	public String deleteFollower(String user_id, String following_user_id) {

		// 팔로워 삭제
		memberDAOService.deleteFollower(following_user_id, user_id);

		// 팔로워 취소 알림 메시지 보내기
		NotificationVO notificationVO = new NotificationVO();
		notificationVO.setNotice_to(following_user_id);
		notificationVO.setNotice_from(user_id);
		notificationVO.setNotice_type("팔로우 거부");
		notificationVO.setContent(user_id + "님께서 회원님의 팔로우를 취소하셨습니다.");
		notificationDAOService.sendNoticeById(notificationVO);

		List<MemberVO> followerList = memberDAOService.getFollower(user_id);
		System.out.println("followerlist + " + followerList.size());
		
		return Integer.toString(followerList.size());
	}

	// (진산) 팔로잉 한 명 추가
	@RequestMapping("/insertFollowing.do")
	public ModelAndView insertFollowing(MemberFollowVO memberFollow, HttpServletRequest request,
			NotificationVO notificaitonVO) {
		String user_id = memberFollow.getUser_id();
		String following_user_id = request.getParameter("following_user_id");
		String user_img = memberFollow.getUser_img();
		String following_user_img = memberFollow.getFollowing_user_img();
		// System.out.println("u=" + user_id);
		// System.out.println("f=" + following_user_id);
		// System.out.println("u-i=" + user_img);
		// System.out.println("f-i=" + following_user_img);
		ModelAndView result = new ModelAndView();
		memberDAOService.insertFollowing(user_id, following_user_id, user_img, following_user_img);

		MemberVO member = memberDAOService.getMember(user_id);
		System.out.println("추가 성공");

		notificationDAOService.sendNoticeById(notificaitonVO);

		// List<MemberFollowVO> recommendList =
		// memberDAOService.getRecommendFollower(user_id);
		// System.out.println("recommendList.size()=" + recommendList.size());
		// result.addObject("recommendList", recommendList);
		result.addObject("member", member);
		result.setViewName("insertFollowing_modal");
		return result;
	}

	// (진산) 좋아요 클릭시 한 개 추가...@ResponseBody를 써서 json오브젝트만 리턴하게 된다
	@RequestMapping("/updateBoardLike.do")
	@ResponseBody
	public String updateBoardLike(BoardVO boardVO, NotificationVO notificaitonVO) {
		// String[] following_user_id= null;
		// ModelAndView result= new ModelAndView();
		String user_id = boardVO.getUser_id();
		int board_num = boardVO.getBoard_num();
		System.out.println("나오자나" + boardVO.getUser_id());
		UserLikeBoVO res = boardDAOService.getUserLike(boardVO);
		System.out.println(user_id);
		System.out.println(board_num);
		if (res == null) {
			boardDAOService.insertUserLike(boardVO);
			boardDAOService.likePlusOne(boardVO);
			System.out.println("좋아요 성공");
			System.out.println(notificaitonVO.getNotice_to());
			notificationDAOService.sendNoticeById(notificaitonVO);
			return "1";
		} else {
			boardDAOService.deleteUserLike(boardVO);
			boardDAOService.likeMinusOne(boardVO);
			System.out.println("좋아요가 중복입니다. 삭제합니다");
			System.out.println(notificaitonVO.getNotice_to());
			notificationDAOService.sendNoticeById(notificaitonVO);
			return "0";
		}
	}

	// 진산 & 성빈 : 맴버 한 명의 냉장고 불러오기..
	@RequestMapping("/getMemberBox.do")
	public String getMyIrdntByUserId(MemberBoxVO memberBoxVO, HttpSession session, Model model) {
		String user_id = (String) session.getAttribute("user_id");
		ArrayList<MemberBoxVO> myIrdntList = memberDAOService.getMyIrdntByUserId(user_id);
		model.addAttribute("myIrdntList", myIrdntList);
		return "myBox";
	}

	// (진산) 냉장고 추가 재료 정하기
	@RequestMapping("/insertMemberBox.do")
	@ResponseBody
	public void insertMemberBox(String[] my_irdnt, HttpSession session) {

		int length = my_irdnt.length;
		String user_id = (String) session.getAttribute("user_id");
		for (int i = 0; i < length; i++) {
			MemberBoxVO memberBoxVO = new MemberBoxVO();
			memberBoxVO.setMy_irdnt(my_irdnt[i]);
			memberBoxVO.setUser_id(user_id);
			memberDAOService.insertIrdnt(memberBoxVO);
		}

	}

	// (성빈) 내 냉장고 재료 삭제
	@RequestMapping("/deleteMemberBox.do")
	@ResponseBody
	public void deleteMemberBox(String my_irdnt, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		MemberBoxVO memberBoxVO = new MemberBoxVO();
		memberBoxVO.setUser_id(user_id);
		memberBoxVO.setMy_irdnt(my_irdnt);
		memberDAOService.deleteIrdnt(memberBoxVO);
	}

	// 성빈 : 레시피 조회하는 페이지(recipe.jsp)의 모델
	@RequestMapping("/recipe.do")
	public String recipe(HttpSession session, Model model) {
		// 로그인 아이디 전달 받기
		String user_id = (String) session.getAttribute("user_id");
		// 로그인 체크, 세션에 아이디를 입력하지 못 했을 경우 로그인 실패로 판단하고 index화면으로 넘긴다
		if (user_id == null) {
			System.out.println("로그인에 실패하셨습니다.");
			session.invalidate();
			return "redirect:/";
		}
		// Header 기본 정보(알림/메시지/유저이미지) 정보 받아오기
		// 내 정보 받아오기
		MemberVO MemberVO = memberDAOService.getUserInfoById(user_id);
		// message 리스트 받아오기
		ArrayList<MessageVO> messageList = messageDAOService.getMyMessageById(user_id);
		// 알림 리스트 받아오기
		ArrayList<NotificationVO> notificationList = notificationDAOService.getMyNoticeById(user_id);
		// 내 냉장고 재료 받아오기
		ArrayList<MemberBoxVO> myIrdntList = memberDAOService.getMyIrdntByUserId(user_id);
		// 모델에 객체 추가
		model.addAttribute("myIrdntList", myIrdntList);
		model.addAttribute("messageList", messageList);
		model.addAttribute("notificationList", notificationList);
		model.addAttribute("MemberVO", MemberVO);

		return "recipe";
	}

	@RequestMapping("/recipeDetail.do")
	public String recipeDetail(HttpSession session, RecipeVO recipeVO, Model model) {
		// (성빈) : 로그인 체크
		// session에 로그인 아이디 없을 경우 초기화면으로 이동
		String user_id = (String) session.getAttribute("user_id");
		if (user_id == null) {
			return "redirect:/";
		}
		// Header에 전해줄 사용자 개인정보
		MemberVO memberVO = memberDAOService.getUserInfoById(user_id);
		// Header에 전해줄 사용자 알림 메시지 리스트
		ArrayList<NotificationVO> notificationList = notificationDAOService.getMyNoticeById(user_id);
		// Header에 전해줄 사용자 개인 메시지 리스트
		ArrayList<MessageVO> messageList = messageDAOService.getMyMessageById(user_id);
		// Header 내 냉장고 재료 받아오기
		ArrayList<MemberBoxVO> myIrdntList = memberDAOService.getMyIrdntByUserId(user_id);

		// recipe_id로 recipe 정보 조회
		RecipeVO recipe = recipeDAOService.getRecipeById(recipeVO);
		// recipe에 있는 user_id로 작성자의 memberVO정보 조회
		MemberVO recipeOwner = memberDAOService.getUserInfoById(recipe.getUser_id());
		// (성빈), contentBoard수정 : recipeVO에 저장된 recipe_id를 이용해 해당 Board 데이터를
		// BoardVO 객체에 담아 리턴하는 메소드
		BoardVO boardVO = boardDAOService.getBoardByRecipeId(recipeVO);
		// (성빈), 추가 : BoardVO의 board_num과 연결된 댓글 리스트를 리턴하는 메소드
		ArrayList<BoardReplyVO> replyList = boardDAOService.getReplyByBoardNum(boardVO);

		// RecipeDetail.jsp에 객체 전달
		model.addAttribute("memberVO", memberVO);
		model.addAttribute("notificationList", notificationList);
		model.addAttribute("messageList", messageList);
		model.addAttribute("recipe", recipe);
		model.addAttribute("recipeOwner", recipeOwner);
		model.addAttribute("boardVO", boardVO);
		model.addAttribute("replyList", replyList);
		model.addAttribute("myIrdntList", myIrdntList);

		return "recipeDetail";
	}

	// 성빈 : recipeDetail 화면에서 댓글 쓸 경우 댓글 추가 및 댓글 알림 메시지 보내기

	@RequestMapping("/writeBoard.do")
	public ModelAndView writeBoard(BoardReplyVO boardReplyVO, String writer) {

		ModelAndView rs = new ModelAndView();
		int board_num = boardReplyVO.getBoard_num();
		if (board_num == 0) {
			System.out.println("SYSTEM  :  댓글 입력 에러 발생, Controller; writeBoard; board_num을 전달받지 못했습니다. "
					+ boardReplyVO.getBoard_num());
			return null;
		}
		// 게시글 작성자에게 댓글 알림 메시지 보내기 (내 게시물이 아닌 경우에만)
		if (!(writer.equals(boardReplyVO.getUser_id()))) {
			NotificationVO notificationVO = new NotificationVO();
			notificationVO.setNotice_to(writer);
			notificationVO.setNotice_from(boardReplyVO.getUser_id());
			notificationVO.setContent(boardReplyVO.getUser_id() + "님께서 회원님의 게시글에 댓글을 남기셨습니다.");
			notificationVO.setNotice_type(Integer.toString(board_num));
			notificationDAOService.sendNoticeById(notificationVO);
		}
		// 인욱
		// 받은 값을 DB에 저장해줌
		boardDAOService.writeBoard(boardReplyVO);
		// 모델엔뷰 이용해서 DB저장값을 뿌려줌
		List<BoardReplyVO> replyList = boardDAOService.getBoard(boardReplyVO);
		rs.addObject("replyList", replyList);
		// 넘겨줄 페이지 이름 기술
		rs.setViewName("replyList");
		return rs;

	}
	
	@RequestMapping(value="/writeBoard.do", params="page=mypage")
	public ModelAndView writeBoardInMypage(BoardReplyVO boardReplyVO, String writer) {
		ModelAndView rs = new ModelAndView();
		
		
		int board_num = boardReplyVO.getBoard_num();
		if (board_num == 0) {
			System.out.println("SYSTEM  :  댓글 입력 에러 발생, Controller; writeBoard; board_num을 전달받지 못했습니다. "
					+ boardReplyVO.getBoard_num());
			return null;
		}
		// 게시글 작성자에게 댓글 알림 메시지 보내기 (내 게시물이 아닌 경우에만)
		if (!(writer.equals(boardReplyVO.getUser_id()))) {
			NotificationVO notificationVO = new NotificationVO();
			notificationVO.setNotice_to(writer);
			notificationVO.setNotice_from(boardReplyVO.getUser_id());
			notificationVO.setContent(boardReplyVO.getUser_id() + "님께서 회원님의 게시글에 댓글을 남기셨습니다.");
			notificationVO.setNotice_type(Integer.toString(board_num));
			notificationDAOService.sendNoticeById(notificationVO);
		}
		
		// 인욱
		// 받은 값을 DB에 저장해줌
		boardDAOService.writeBoard(boardReplyVO);
				
		List<BoardReplyVO> replyList = boardDAOService.getBoard(boardReplyVO);
		rs.addObject("replyList", replyList);
		// 넘겨줄 페이지 이름 기술
		rs.setViewName("replyList2");
		
		return rs;

	}

	// 주석 쓸 때 자기 이름 꼭 써요
	// (성빈) 레시피 저장 모델
	// MF로 파일 객체를 읽어 파일을 로컬 디렉토리에 저장한 뒤 해당 URL을 리턴받아
	// recipeVO에 입력한 뒤 recipeVO를 DB에 저장한다.
	@RequestMapping("/recipeInsert.do")
	public String recipeInsert(RecipeVO recipeVO, HttpSession session, @RequestParam("titleImg") MultipartFile titleImg,
			@RequestParam("stepImg") MultipartFile[] stepImg) throws Exception {

		System.out.println("SYSTEM  :  레시피 입력 시작, 입력 받은 과정 목록의 개수는 총 " + recipeVO.getCooking_desc().length);
		System.out.println("SYSTEM  :  레시피 입력 시작, 입력 받은 파일 객체의 개수는 총 " + stepImg.length);
		if (recipeVO.getCooking_desc().length != stepImg.length) {
			System.out.println("SYSTEM  :  레시피 입력 실패, 입력 받은 과정 목록의 개수와 stepImg 파일 객체의 개수가 같지 않습니다.");
			return null;
		}
		ModelAndView mav = new ModelAndView();
		// (성빈) 작성자의 id 값 읽어오기
		String user_id = (String) session.getAttribute("user_id");
		// (성빈) titleImg를 Local Directory에 저장하고 저장경로를 리턴받는 메소드
		String img_url = recipeDAOService.getImgURLByImgFile(titleImg);
		recipeVO.setImg_url(img_url);
		recipeVO.setUser_id(user_id);
		// (성빈) stepImg를 Local Directory에 저장하고 저장한 경로를 배열 형태로 리턴받는 메소드
		String[] step_img_url = recipeDAOService.getImgURLByImgFiles(stepImg);
		recipeVO.setStep_img_url(step_img_url);

		// (성빈) recipeVO에 입력되어 있는 recipe 기본정보를 recipe_Info 테이블에 INSERT
		recipeDAOService.insertRecipe(recipeVO);
		// (성빈) recipeVO에 저장되어 있는 recipe 재료정보를 recipe_Irdnt 테이블에 INSERT
		ArrayList<RecipeIrdntVO> recipeIrdntList = recipeDAOService.getIrdntVOFromRecipeVO(recipeVO);
		for (int i = 0; i < recipeIrdntList.size(); i++) {
			recipeDAOService.insertRecipeIrdnt(recipeIrdntList.get(i));
		}
		// (성빈) recipeVO에 저장되어 있는 recipe 과정정보를 recipe_Process 테이블에 INSERT
		ArrayList<RecipeProcessVO> recipeProcessList = recipeDAOService.getProcessVOFromRecipeVO(recipeVO);
		for (int i = 0; i < recipeProcessList.size(); i++) {
			recipeDAOService.insertRecipeProcess(recipeProcessList.get(i));
		}
		// (성빈) 파라메터 전달 테스트 성공, 각각의 순서에 맞춰 스트링 배열이 얻어짐을 확인
		/*
		 * System.out.println("레시피 입력 테스트 시작");
		 * System.out.println("RecipeInfo 기본 정보 입력 결과 확인 시작....");
		 * System.out.println("recipe_id : " + recipeVO.getRecipe_id());
		 * System.out.println("recipe_title : " + recipeVO.getRecipe_title());
		 * System.out.println("recipe_desc : " + recipeVO.getRecipe_desc());
		 * System.out.println("nation_code : " + recipeVO.getNation_code());
		 * System.out.println("ty_code : " + recipeVO.getTy_code());
		 * System.out.println("cooking_time : " + recipeVO.getCooking_time());
		 * System.out.println("img_url : " + recipeVO.getImg_url());
		 * System.out.println("detail_url : " + recipeVO.getDetail_url());
		 * System.out.println("MultipartFile titleImg에 입력되어 있는 사진의 이름은 " +
		 * titleImg.getOriginalFilename());
		 * System.out.println("MultipartFile titleImg에 입력되어 있는 사진의 크기는 " +
		 * titleImg.getSize());
		 * 
		 * System.out.println("RecipeIrdnt 기본 정보 입력 결과 확인 시작...."); String[]
		 * irdnt_name = recipeVO.getIrdnt_name(); for (int i = 0; i <
		 * irdnt_name.length; i++) { System.out.println("입력된 총 재료명의 개수는 " +
		 * irdnt_name.length); System.out.println(i + "번 째 irdnt_name : " +
		 * irdnt_name[i]); } String[] irdnt_cpcty = recipeVO.getIrdnt_cpcty();
		 * for (int i = 0; i < irdnt_cpcty.length; i++) {
		 * System.out.println("입력된 총 재료용량의 개수는 " + irdnt_cpcty.length);
		 * System.out.println(i + "번 째 irdnt_cpcty : " + irdnt_cpcty[i]); }
		 * int[] irdnt_ty_code = recipeVO.getIrdnt_ty_code(); for (int i = 0; i
		 * < irdnt_ty_code.length; i++) { System.out.println("입력된 총 재료타입의 개수는 "
		 * + irdnt_ty_code.length); System.out.println(i +
		 * "번 째 irdnt_ty_code : " + irdnt_ty_code[i]); }
		 * System.out.println("입력된 재료명, 재료용량, 재료타입의 개수가 모두 동일하고, 각각의 순서가 맞는가 ?"
		 * );
		 * 
		 * System.out.println("RecipeProcess 기본 정보 입력 결과 확인 시작"); int[]
		 * cooking_no = recipeVO.getCooking_no(); for (int i = 0; i <
		 * cooking_no.length; i++) { System.out.println("입력된 총 과정순서의 개수는 " +
		 * cooking_no.length); System.out.println(i + "번 째 cooking_no : " +
		 * cooking_no[i]); } String[] cooking_desc = recipeVO.getCooking_desc();
		 * for (int i = 0; i < cooking_desc.length; i++) {
		 * System.out.println("입력된 총 과정설명의 개수는 " + cooking_desc.length);
		 * System.out.println(i + "번 째 cooking_desc : " + cooking_desc[i]); }
		 * String[] step_tip = recipeVO.getStep_tip(); for (int i = 0; i <
		 * step_tip.length; i++) { System.out.println("입력된 총 과정 팁의 개수는 " +
		 * step_tip.length); System.out.println(i + "번 째 step_tip : " +
		 * step_tip[i]); } String[] step_img_url = recipeVO.getStep_img_url();
		 * if(step_img_url != null) { for (int i = 0; i < step_img_url.length;
		 * i++) { System.out.println("입력된 총 과정 사진의 개수는 " + step_img_url.length);
		 * System.out.println(i + "번 째 step_img_url : " + step_img_url[i]); } }
		 * System.out.println("stepImg 파일 입력 체크 시작"); for (int i = 0; i <
		 * stepImg.length; i++) { System.out.println("입력된 총 과정 사진 파일의 개수는 " +
		 * stepImg.length); System.out.println(i + "번 째 stepImg : " +
		 * stepImg[i].getOriginalFilename()); } System.out.
		 * println("입력된 과정순서, 과정설명, 과정 팁, 과정 사진의 개수가 모두 동일하고, 각각의 순서가 맞는가?");
		 */

		// 파일 업로드 안돼요 오빠가 하셔야해요
		/*
		 * String name = request.getParameter("img_url"); MultipartFile mf =
		 * request.getFile("file");
		 * 
		 * //String uploadPath =
		 * request.getSession().getServletContext().getRealPath("/upload");
		 * String uploadPath = "C://upload//";//직접 업로드할 위치 지정 String
		 * originalFileExtension =
		 * mf.getOriginalFilename().substring(mf.getOriginalFilename().
		 * lastIndexOf(".")); String storedFileName =
		 * UUID.randomUUID().toString().replaceAll("-", "") +
		 * originalFileExtension;
		 * 
		 * //지정한 주소에 파일 저장 if(mf.getSize() != 0) { //mf.transferTo(new
		 * File(uploadPath+"/"+mf.getOriginalFilename())); mf.transferTo(new
		 * File(uploadPath+storedFileName)); }
		 * 
		 * recipeDAOService.insertRecipe(recipeVO);
		 */

		/*
		 * BoardVO boardVO = new BoardVO();
		 * 
		 * // boardVO.setUser_id(recipeVO.getUser_id());
		 * boardVO.setBoard_title(recipeVO.getRecipe_title());
		 * boardVO.setBoard_summry(recipeVO.getRecipe_desc());
		 * boardVO.setBoard_img(recipeVO.getImg_url());
		 * boardVO.setBoard_recipe_id(recipeVO.getRecipe_id());
		 * 
		 * boardDAOService.insertBoard(boardVO);
		 * 
		 * // mav.addObject("fileName", mf.getOriginalFilename());
		 * mav.setViewName("redirect:/postList.do");
		 */
		boardDAOService.insertBoard(recipeVO);

		return "redirect:/postList.do";

	}


	@RequestMapping("/test.do")
	public String test()
	{
		
		return "test";
	}
	//카카오 로그인정보 체크 (이미 있는 이메일 인지 없는 이메일 인지)
	@RequestMapping("/checkMember.do")
	public ModelAndView checkMember(MemberVO memberVO, HttpSession session)
	{	
		
		ModelAndView model = new ModelAndView();
		
		
		System.out.println(memberVO.getUser_id());
		System.out.println(memberVO.getUser_name());
		System.out.println(memberVO.getUser_img());
		
		//카카오 아이디, 이미지 저장
		String kakao_id = memberVO.getUser_id();
		String kakao_img = memberVO.getUser_img();
		
		// 여기 x 는 간편로그인 창에서 이메일중복체크시 x 값을 다시 받기떄문에 임시로 넘겨준 x에요.
		int x = 0;		
		//로그인 정보가 들어오면 이메일 확인 후 x에 저장
		MemberVO xx = memberDAOService.getUserInfoById(memberVO.getUser_id());
		//MemberVO x = memberDAOService.checkMember(memberVO);
		
		//db에서 확인한 id 값이 null이면 간단회원가입 페이지로, 
		//null이 아니면  카카오 아이디,이메일 가지고 (recipe 페이지로)
		
		if(xx == null){
			//mav.addObject("sns_join", x);
			model.addObject("kakao_id", kakao_id);
			model.addObject("kakao_img", kakao_img);
			model.addObject("x",x);
			model.setViewName("sns_join");
			return model;
			
		}
			// Header에 들어가야 할 기본 데이터 읽어오기
			// 내 알림 메시지 읽어오기
			ArrayList<NotificationVO> notificationList = notificationDAOService.getMyNoticeById(xx.getUser_id());
			// 내 개인 메시지 읽어오기
			ArrayList<MessageVO> messageList = messageDAOService.getMyMessageById(xx.getUser_id());
			// 내 개인정보 읽어오기
			MemberVO memVO= memberDAOService.getMember(xx.getUser_id());
			// 내 냉장고 재료 목록 불러오기
			ArrayList<MemberBoxVO> myIrdntList = memberDAOService.getMyIrdntByUserId(xx.getUser_id());
			
			//헤더정보 모델에 저장
			model.addObject("messageList", messageList);
			model.addObject("notificationList", notificationList);
			model.addObject("MemberVO", memVO);
			//카카오 아이디, 이메일만 저장
			
			//session.setAttribute("user_id", kakao_id);
			
			model.setViewName("redirect:/recipe.do");	
			//아이디정보 세션 저장
			//세션으로 저장해서 전달해 주기 session.getAttribute 로 값 받을수 있어요
			session.setAttribute("user_id", xx.getUser_id());
			session.setAttribute("user_email", xx.getUser_email());
			session.setAttribute("user_name", xx.getUser_name());
			session.setAttribute("user_img", xx.getUser_img());
			
			
			return model;
	}
				
	
	/*@RequestMapping("/snsJoin.do")
	public String snsJoin() {
		
		
		
		return "recipe";
		
	}*/
	
	
	
	//카카오톡 로그아웃 (리다이렉트 -> 메인페이지)
	@RequestMapping("/kakaologout.do")
	public String logout(HttpSession session){
		session.invalidate();
		
		return "redirect:/test.do";
	}
	// 성빈 : 카테고리 입력 값에 따른 레시피 목록 출력 메소드
	// 비동기 통신(ajax)에 의해 호출되는 메소드
	// return으로 recipeListByCateogry를 지정해 recipeListByCategory를 뷰로 보여주지 않고
	// 해당 뷰의 결과화면 recipe.jsp에 출력한다
	@RequestMapping(value = "/recipe.do", params = "sb=ty_code")
	public String getRecipesByCategory(String[] ty_code, Model model, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		ArrayList<RecipeVO> recipeList = null;
		ArrayList<UserLikeBoVO> likeRecipeList = boardDAOService.getUserLikeBoardList(user_id);
		if (ty_code == null) {
			model.addAttribute("recipeList", recipeList);
			return "recipeListByCategory";
		} else {
			recipeList = recipeDAOService.getRecipesByTy_code(ty_code);
			model.addAttribute("recipeList", recipeList);
			return "recipeListByCategory";
		}
	}

	// 성빈 : 재료 입력 값에 따른 레시피 목록 출력 메소드
	// 비동기 통신에 의해 호출되는 메소드
	// 결과를 recipeListByIngredients에 보여주고 비동기 통신에 의해
	// 해당 결과를 recipe.jsp에 출력한다.
	@RequestMapping(value = "/recipe.do", params = "sb=irdnt")
	public String getRecipesByIngredients(String main, String sub, String source, Model model) {
		SearchEngine se = new SearchEngine();
		ArrayList<RecipeVO> recipeList = se.searchRecipesByIngredients(main, sub, source);
		if (recipeList.size() == 0) {
			recipeList = null;
		} else {
			recipeList = se.getRecipesBySearch(recipeList);
		}
		model.addAttribute("recipeList", recipeList);
		return "recipeListByIngredients";
	}

	// 성빈 : 내 냉장고 입력 값에 따른 레시피 목록 출력 메소드
	// 비동기 통신에 의해 호출되는 메소드
	// 결과를 recipeListByMybox에 보여주고 비동기 통신에 의해
	// recipe.jsp에 출력한다
	@RequestMapping(value = "/recipe.do", params = "sb=mybox")
	public String getRecipesByMybox(Model model, HttpSession session, HttpServletResponse response) {
		String user_id = (String) session.getAttribute("user_id");
		SearchEngine se = new SearchEngine();
		ArrayList<MemberBoxVO> myIrdntList = memberDAOService.getMyIrdntByUserId(user_id);
		ArrayList<RecipeVO> recipeList = null;
		if (myIrdntList.size() == 0) {
			response.setContentType("text/html; charset=UTF-8");
			response.setCharacterEncoding("utf-8");
			try {
				PrintWriter writer = response.getWriter();
				writer.write("<script>");
				;
				writer.write("alert('현재 내 냉장고가 비어있습니다.');");
				writer.write("</script>");
				model.addAttribute("recipeList", recipeList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			recipeList = se.searchRecipeListByMybox(myIrdntList);
			recipeList = se.getRecipeListByMybox(recipeList);
		}
		model.addAttribute("recipeList", recipeList);
		return "recipeListByMybox";
	}

	// 성빈 : 메시지 전송 작업을 처리하기 위한 모델
	// 메시지 삭제 시 url 변동을 막기 위해 이 url 요청은 ajax를 통해 요청해야 합니다.
	// ResponseBody는 요청의 응답 결과를 뷰로 연결시키는 것이 아니라 특정 객체를 리턴할 수 있게 해주는
	// Spring 어노테이션으로 여기선 리턴값이 void이므로 어떤 객체(데이터)도 응답하지 않습니다.
	// DB에서 단지 check값을 수정만 해주면 되기 때문에
	// 화면 상에서는 해당 엘리먼트의 display 값을 none으로 주어 사라지는 효과를 주면 됩니다.
	@RequestMapping(value = "/updateMessage.do")
	@ResponseBody
	public void updateMessage(String message_id) {

		messageDAOService.removeMessageByMessageId(message_id);
		return;

	}

	// 성빈 : 알림 메시지 전송 작업을 처리하기 위한 모델
	// 메시지 삭제 시 url 변동을 막기 위해 이 url 요청은 ajax를 통해 요청해야 합니다.
	// ResponseBody는 요청의 응답 결과를 뷰로 연결시키는 것이 아니라 특정 객체를 리턴할 수 있게 해주는
	// Spring 어노테이션으로 여기선 리턴값이 void이므로 어떤 객체(데이터)도 응답하지 않습니다.
	// DB에서 단지 check값을 수정만 해주면 되기 때문에
	// 화면 상에서는 해당 엘리먼트의 display 값을 none으로 주어 사라지는 효과를 주면 됩니다.
	@RequestMapping(value = "/updateNotification.do")
	@ResponseBody
	public void updateNotification(String notice_id, String followAdmit) {
		// notice_id로 notice_type을 식별
		NotificationVO notificationVO = notificationDAOService.getMyNoticeByNoticeId(notice_id);
		// 팔로우 신청의 경우 팔로우 신청에 대한 처리를 해주어야 함.
		if (notificationVO.getNotice_type().equals("팔로우 신청")) {
			if (followAdmit.equals("수락")) {
				// 팔로우 받아주기 (member_follow테이블에 user_id에는 신청한 사람,
				// following_user_id에는 나)
				MemberFollowVO memberFollowVO = new MemberFollowVO();
				String user_id = notificationVO.getNotice_from();
				String following_user_id = notificationVO.getNotice_to();
				String user_img = memberDAOService.getUserInfoById(user_id).getUser_img();
				String following_user_img = memberDAOService.getUserInfoById(following_user_id).getUser_img();
				memberFollowVO.setUser_id(user_id);
				memberFollowVO.setFollowing_user_id(following_user_id);
				memberFollowVO.setUser_img(user_img);
				memberFollowVO.setFollowing_user_img(following_user_img);
				// 팔로우 추가 (member_follow 테이블에 데이터 추가)
				memberDAOService.setFollowing(memberFollowVO);
				// 해당 알림 메시지 업데이트
				notificationDAOService.removeNoticeByNoticeId(notice_id);
				// 팔로우 신청한 사람에게 수락되었다고 메시지 보내기
				NotificationVO acceptNotice = new NotificationVO();
				acceptNotice.setNotice_to(notificationVO.getNotice_from());
				acceptNotice.setNotice_from(notificationVO.getNotice_to());
				acceptNotice.setContent(notificationVO.getNotice_to() + "님께서 회원님의 팔로우 신청을 수락하셨습니다");
				acceptNotice.setNotice_type("팔로우 수락");
				// 해당 알림메시지 보내기
				notificationDAOService.sendNoticeById(acceptNotice);
				return;
			} else {
				System.out.println("팔로우 신청 거절을 인식하는가");
				// 팔로우 거절할 경우 팔로우 거부 되었다고 알림 메시지 보내기
				NotificationVO rejectNotice = new NotificationVO();
				rejectNotice.setNotice_to(notificationVO.getNotice_from());
				rejectNotice.setNotice_from(notificationVO.getNotice_to());
				rejectNotice.setContent(notificationVO.getNotice_to() + "님께서 회원님의 팔로우 신청을 거부하셨습니다");
				rejectNotice.setNotice_type("팔로우 거부");
				// 해당 알림메시지 보내기
				notificationDAOService.sendNoticeById(rejectNotice);
				// 기존의 신청 알림 업데이트
				notificationDAOService.removeNoticeByNoticeId(notice_id);
				return;
			}
			// 팔로우 신청이 아닌 다른 알림인 경우 좋아요, 댓글 알림인 경우 알림만 업데이트
		} else {
			notificationDAOService.removeNoticeByNoticeId(notice_id);
			return;
		}
	}

	@RequestMapping(value = "/notification.do")
	public String getNotification(Model model, String user_id) {
		ArrayList<NotificationVO> notificationList = notificationDAOService.getMyNoticeById(user_id);
		model.addAttribute("notificationList", notificationList);
		return "notification";

	}

	// 성빈 : 안 읽은 메시지를 읽어오는 메소드
	@RequestMapping(value = "/message.do")
	public String getMessage(Model model, String user_id) {
		ArrayList<MessageVO> messageList = messageDAOService.getMyMessageById(user_id);
		model.addAttribute("messageList", messageList);
		return "message";
	}

	// 성빈 : recipeDetail에서 게시판 좋아요 눌렀을 때 처리 하는 메소드
	@RequestMapping("/likeBoard.do")
	@ResponseBody
	public String likeBoard(UserLikeBoVO userLikeBoVO, String writer) {
		// 좋아요 누른 적 있는지 없는지 판단
		UserLikeBoVO vo = boardDAOService.doesAlreadyLike(userLikeBoVO);
		if (vo == null) {
			System.out.println("SYSTEM  :  UserLikeBo Select 결과 데이터가 존재하지 읺아 UserLikeVO가 null이 리턴되었습니다.");
			System.out.println("SYSTEM  :  UserLikeBo 에 데이터를 추가합니다. user_id : " + userLikeBoVO.getUser_id()
					+ "  board_num : " + userLikeBoVO.getBoard_num());
			// Board 테이블에 좋아요 카운트 증가
			boardDAOService.updateBoardLikePlus(userLikeBoVO);
			// 좋아요 알림 메시지 전송
			NotificationVO notificationVO = new NotificationVO();
			notificationVO.setNotice_to(writer);
			notificationVO.setNotice_from(userLikeBoVO.getUser_id());
			String content = userLikeBoVO.getUser_id() + "님이 회원님의 게시물을 좋아합니다.";
			String notice_type = "좋아요 추가";
			notificationVO.setContent(content);
			notificationVO.setNotice_type(notice_type);
			notificationDAOService.sendNoticeById(notificationVO);
			System.out.println(
					"SYSTEM  :  " + writer + "에게 좋아요 알림 메시지를 보냈습니다" + "  FROM  :  " + userLikeBoVO.getUser_id());
			// 좋아요 테이블에 추가
			boardDAOService.insertUserLikeBo(userLikeBoVO);
			return "1";
		} else {
			// 좋아요 감소
			boardDAOService.updateBoardLikeMinus(userLikeBoVO);
			// 좋아요 테이블 삭제
			boardDAOService.deleteUserLikeBo(userLikeBoVO);
			return "0";
		}
	}

	// 성빈 : recipeDetail에서 게시판 댓글 삭제 버튼 눌렀을 때 처리하는 메소드
	@RequestMapping("/deleteReply.do")
	@ResponseBody
	public String deleteReply(BoardReplyVO boardReplyVO, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		boardDAOService.deleteReply(boardReplyVO);

		return Integer.toString(boardDAOService.getReplySizeByBoardNum(boardReplyVO));
	}

	// 성빈 : recipeDetail에서 팔로우 버튼 눌렀을 때 처리하는 메소드
	// 팔로우 요청 알림을 발생시킨다
	@RequestMapping("/followRequest.do")
	@ResponseBody
	public String followRequest(MemberFollowVO memberFollowVO) {
		int checkFollowing = memberDAOService.checkFollowing(memberFollowVO);
		if (checkFollowing != 0) {
			System.out.println("SYSTEM  :  현재 팔로우한 회원을 다시 팔로우할 순 없습니다. 팔로우 요청을 취소합니다. 요청자 : "
					+ memberFollowVO.getUser_id() + " 요청 대상 : " + memberFollowVO.getFollowing_user_id());
			return "0";
		}
		int checkFollowRequest = notificationDAOService.checkFollowRequest(memberFollowVO);
		if(checkFollowRequest != 0) {
			System.out.println("SYSTEM  :  현재 팔로우 요청 읍답 대기 중입니다. 팔로우 요청을 취소합니다. 요청자 : " + memberFollowVO.getUser_id() + " 요청 대상 : " + memberFollowVO.getFollowing_user_id());
			return "1";
		}
		NotificationVO followRequest = new NotificationVO();
		followRequest.setNotice_to(memberFollowVO.getFollowing_user_id());
		followRequest.setNotice_from(memberFollowVO.getUser_id());
		followRequest.setNotice_type("팔로우 신청");
		followRequest.setContent(memberFollowVO.getUser_id() + "님께서 팔로우를 신청하셔습니다.");
		notificationDAOService.sendNoticeById(followRequest);
		return "2";
	}
/*
	@RequestMapping("/snsJoin.do")
	public String snsJoin() {

		return "sns_join";

<<<<<<< HEAD
	}*/

	
	@RequestMapping("/sendMessage.do")
	public String sendMessage(MessageVO messageVO)
	{
		messageDAOService.sendMessageToId(messageVO);
		
		System.out.println(messageVO.getUser_id());
		System.out.println(messageVO.getContent());
		System.out.println(messageVO.getSender_id());
		
		
		return "redirect:/recipe.do";
	}

	//(예진) 내 정보 보기
	@RequestMapping("/myInfo.do")
	public ModelAndView getMyInfo(BoardVO boardVO, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		ModelAndView result = new ModelAndView();
		MemberVO vo = memberDAOService.getMember(user_id);
		System.out.println("user" + user_id);
		result.addObject("user", vo);
		result.setViewName("myInfo");
		return result;
	}

	// 예진/회원 정보 수정
	@RequestMapping("/updateInfo.do")
	public String updateInfo(MemberVO memberVO, HttpSession session) {
		memberVO.setUser_id((String) session.getAttribute("user_id"));

		memberDAOService.updateInfo(memberVO);

		return "redirect:/mypage.do";
	}

	// 성빈
	// 상단 헤더 바 검색 시 처리하는 모델
	@RequestMapping("/search.madi")
	public String searchMadi(HttpSession session, Model model, String query) {

		// 로그인 아이디 전달 받기
		String user_id = (String) session.getAttribute("user_id");
		// 로그인 체크, 세션에 아이디를 입력하지 못 했을 경우 로그인 실패로 판단하고 index화면으로 넘긴다
		if (user_id == null) {
			System.out.println("SYSTEM  :  접속이 해제 되었거나 비정상적 접근입니다. 초기화면으로 이동합니다.");
			session.invalidate();
			return "redirect:/";
		}
		// Header 기본 정보(알림/메시지/유저이미지) 정보 받아오기
		// 내 정보 받아오기
		MemberVO MemberVO = memberDAOService.getUserInfoById(user_id);
		// message 리스트 받아오기
		ArrayList<MessageVO> messageList = messageDAOService.getMyMessageById(user_id);
		// 알림 리스트 받아오기
		ArrayList<NotificationVO> notificationList = notificationDAOService.getMyNoticeById(user_id);
		// 내 냉장고 재료 받아오기
		ArrayList<MemberBoxVO> myIrdntList = memberDAOService.getMyIrdntByUserId(user_id);
		// 모델에 객체 추가
		model.addAttribute("myIrdntList", myIrdntList);
		model.addAttribute("messageList", messageList);
		model.addAttribute("notificationList", notificationList);
		model.addAttribute("MemberVO", MemberVO);

		// 검색어 처리
		WebSearch webSearch = new WebSearch();
		SearchEngine searchEngine = new SearchEngine();
		System.out.println("SYSTEM  :  검색어를 입력받았습니다. 입력값은 " + query + " || 형태소 분석을 시작합니다. ");
		List<String> nounList = webSearch.getNounFromText(query);
		String searchSQL = searchEngine.getSearchQueryByNoun(nounList);
		List<Integer> recipeList = searchEngine.getRecipeIdByNoun(searchSQL);
		System.out.println("SYSTEM  :  총 " + recipeList.size() + "개의 검색결과를 얻었습니다. 해당 레시피 정보를 수집합니다.");
		ArrayList<RecipeVO> recipe = new ArrayList<RecipeVO>();
		for (Integer recipe_id : recipeList) {
			RecipeVO recipeVO = new RecipeVO();
			recipeVO.setRecipe_id(recipe_id);
			recipe.add(recipeDAOService.getRecipeById(recipeVO));
		}
		System.out.println("SYSTEM  :  레시피 정보 수집에 성공했습니다. 화면에 노출시킵니다.");
		model.addAttribute("recipe", recipe);
		
		return "searchResult";
	}
	// 성빈 : 
	// SearchResult에서 띄어줄 사이드 바 만들어 주는 모델
	@RequestMapping(value="/search.madi", params="side=active")
	public String searchMadi(RecipeVO recipeVO, Model model) {
		System.out.println("SYSTEM  :  " + recipeVO.getRecipe_id() + "에 대한 SideBar 데이터를 생성합니다");
		RecipeVO result = recipeDAOService.getRecipeById(recipeVO);
		System.out.println("SYSTEM  :  " + recipeVO.getRecipe_id() + "에 대한 SideBar 데이터를 생성 성공. 화면에 노출시킵니다.");
		model.addAttribute("recipeVO", result);
		return "searchSide";
	}
/*	
	// 예진: 내 정보 보기
	@RequestMapping("/myInfo.do")
	public ModelAndView getMyInfo(BoardVO boardVO, HttpSession session) {
		String user_id = (String)session.getAttribute("user_id");
		ModelAndView result= new ModelAndView();
		MemberVO vo = memberDAOService.getMember(user_id);
		System.out.println("user" + user_id);
		result.addObject("user", vo);
		result.setViewName("myInfo");
		return result;
	}
	
	// 예진: 회원 정보 수정
	@RequestMapping("/updateInfo.do")
	public String updateInfo(MemberVO memberVO, HttpSession session) {		
		memberVO.setUser_id((String)session.getAttribute("user_id"));
		memberDAOService.updateInfo(memberVO);			
		return "redirect:/mypage.do";
	}
	*/
	// 예진: 설정탭
	@RequestMapping("/setting.do")
	public String setting() {
		return "setting";
	}
	
	// 예진: 차단
	@RequestMapping("/blockMember.do")
	public String blockMember(BlockMemberVO blockVO) {
		
		//ID 차단
		int result = memberDAOService.insertBlockMember(blockVO);
		if(result > 0 ) memberDAOService.deleteBlockMember(blockVO);
		return "redirect:/mypage.do";
	}
	
	// 예진: 차단리스트
	@RequestMapping("/getBlockMember.do")
	public ModelAndView getBlockMember(MemberVO memberVO) {
		List<BlockMemberVO> list = memberDAOService.getBlockMember(memberVO.getUser_id());
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("blockList", list);
		mav.setViewName("blockMemberList");
		
		return mav;
	}
	
	//예진: 비밀번호 찾기 화면
	@RequestMapping("/forgotPassword.do")
	public String forgotPassword() {
		return "forgotPassword";
	}
	
	//예진: 임시비밀번호 메일 발송
	@RequestMapping("/sendMail.do")
	public ModelAndView sendMailProcess(
		@RequestParam(value="userEmail",required=false,defaultValue="") String userEmail ) {
		ModelAndView mav = new ModelAndView();
	
		//비밀번호 난수 발생(8자리)
		Random rnd = new Random();
		StringBuffer tempPassword = new StringBuffer();
		 
		for (int i = 0; i < 8; i++) {
		    if (rnd.nextBoolean()) {
		    	tempPassword.append((char)((int)(rnd.nextInt(26)) + 97));
		    }else{
		    	tempPassword.append((rnd.nextInt(10))); 
		    }
		}
		
		if (memberDAOService.existEmail(userEmail) > 0) {
			if(sendEmail(userEmail, tempPassword)){
				memberDAOService.tempPassword(tempPassword.toString(), userEmail);
				mav.addObject("code", "0");
				mav.addObject("message", "메일이 전송되었습니다.");
			}else{
				mav.addObject("code", "-1");
				mav.addObject("message", "메일이 실패되었습니다.");
			}
		} else {
			mav.addObject("code", "-2");
			mav.addObject("message", "존재하는 메일이 아닙니다.");
		}
				
		mav.setViewName("forgotPassword");
		
		return mav;
	}

	// 예진: 메일발송 메소드
	private Boolean sendEmail(String userEmail, StringBuffer tempPassword) {
		Boolean result = false;
		String setfrom = "madiproject03@gmail.com";         
	    String tomail  = userEmail;     // 받는 사람 이메일
	    String title   = "[madi] 마디에서 임시 비밀번호를 보내드립니다.";      // 제목
	    String content = "임시비밀번호: " + tempPassword + "\n로그인 후 비밀번호흘 변경해 주시기 바랍니다.";    // 내용
	   
	    try {
	      MimeMessage message = mailSender.createMimeMessage();
	      MimeMessageHelper messageHelper 
	                        = new MimeMessageHelper(message, true, "UTF-8");
	 
	      messageHelper.setFrom(setfrom);  // 보내는사람 생략하거나 하면 정상작동을 안함
	      messageHelper.setTo(tomail);     // 받는사람 이메일
	      messageHelper.setSubject(title); // 메일제목은 생략이 가능하다
	      messageHelper.setText(content);  // 메일 내용
	     
	      mailSender.send(message);
	      result = true;
	    } catch(Exception e){
	      System.out.println(e);
	    }
	    return result;
	}

	//
	// (진산) 임시..웹소켓 기능
	// @RequestMapping("/chat.do")
	// public String chat() {
	// return "chat";
	// }
	//
}

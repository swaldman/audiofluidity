package audiofluidity.rss

import Element.Itunes

// see https://help.apple.com/itc/podcasts_connect/#/itc9267a2f12
// we're relying on scala-xml to escape out ampersands
object ItunesCategory:
  private def ic(cat : String)                  = Itunes.Category(cat)
  private def ic(cat : String, subcat : String) = Itunes.Category(cat, Some(Itunes.Category(subcat)))

  val Arts                 = ic("Arts")
  val Arts_Books           = ic("Arts", "Books")
  val Arts_Design          = ic("Arts", "Design")
  val Arts_Fashion_Beauty  = ic("Arts", "Fashion & Beauty")
  val Arts_Food            = ic("Arts", "Food")
  val Arts_Performing_Arts = ic("Arts", "Performing Arts")
  val Arts_Visual_Arts     = ic("Arts", "Visual Arts")

  val Business                  = ic("Business")
  val Business_Careers          = ic("Business","Careers")
  val Business_Entrepreneurship = ic("Business","Entrepreneurship")
  val Business_Investing        = ic("Business", "Investing")
  val Business_Management       = ic("Business", "Management")
  val Business_Marketing        = ic("Business", "Marketing")
  val Business_NonProfit        = ic("Business", "Non-Profit")

  val Comedy            = ic("Comedy")
  val Comedy_Interviews = ic("Comedy", "Comedy Interviews")
  val Comedy_Improv     = ic("Comedy","Improv")
  val Comedy_StandUp    = ic("Comedy","Stand-Up")
  val Comedy_Comedy_Interviews = Comedy_Interviews

  val Education                   = ic("Education")
  val Education_Courses           = ic("Education","Courses")
  val Education_How_To            = ic("Education","How To")
  val Education_Language_Learning = ic("Education","Language Learning")
  val Education_SelfImprovement   = ic("Education","Self-Improvement")

  val Fiction = ic("Fiction")
  val Fiction_Comedy_Fiction = ic("Fiction","Comedy Fiction")
  val Fiction_Drama = ic("Fiction","Drama")
  val Fiction_Science_Fiction = ic("Fiction","Science Fiction")

  val Government = ic("Government")

  val History = ic("History")

  val Health_Fitness                    = ic("Health & Fitness")
  val Health_Fitness_Alternative_Health = ic("Health & Fitness", "Alternative Health")
  val Health_Fitness_Fitness            = ic("Health & Fitness", "Fitness")
  val Health_Fitness_Medicine           = ic("Health & Fitness", "Medicine")
  val Health_Fitness_Mental_Health      = ic("Health & Fitness", "Mental Health")
  val Health_Fitness_Nutrition          = ic("Health & Fitness","Nutrition")
  val Health_Fitness_Sexuality          = ic("Health & Fitness","Sexuality")

  val Kids_Family                    = ic("Kids & Family")
  val Kids_Family_Education_For_Kids = ic("Kids & Family","Education For Kids")
  val Kids_Family_Parenting          = ic("Kids & Family","Parenting")
  val Kids_Family_Pets_Animals       = ic("Kids & Family","Pets & Animals")
  val Kids_Family_Stories_For_Kids   = ic("Kids & Family","Stories For Kids")

  val Leisure                 = ic("Leisure")
  val Leisure_Animation_Manga = ic("Leisure", "Animation & Manga")
  val Leisure_Automotive      = ic("Leisure","Automotive")
  val Leisure_Aviation        = ic("Leisure","Aviation")
  val Leisure_Crafts          = ic("Leisure","Crafts")
  val Leisure_Games           = ic("Leisure","Games")
  val Leisure_Hobbies         = ic("Leisure","Hobbies")
  val Leisure_Home_Garden     = ic("Leisure","Home & Garden")
  val Leisure_Video_Games     = ic("Leisure","Video Games")

  val Music                  = ic("Music")
  val Music_Music_Commentary = ic("Music","Music Commentary")
  val Music_Music_History    = ic("Music","Music History")
  val Music_Music_Interviews = ic("Music","Music Interviews")

  val News                    = ic("News")
  val News_Business_News      = ic("News","Business News")
  val News_Daily_News         = ic("News","Daily News")
  val News_Entertainment_News = ic("News","Entertainment News")
  val News_News_Commentary    = ic("News","News Commentary")
  val News_Politics           = ic("News","Politics")
  val News_Sports_News        = ic("News","Sports News")
  val News_Tech_News          = ic("News","Tech News")

  val Religion_Spirituality              = ic("Religion & Spirituality")
  val Religion_Spirituality_Buddhism     = ic("Religion & Spirituality","Buddhism")
  val Religion_Spirituality_Christianity = ic("Religion & Spirituality","Christianity")
  val Religion_Spirituality_Hinduism     = ic("Religion & Spirituality","Hinduism")
  val Religion_Spirituality_Islam        = ic("Religion & Spirituality","Islam")
  val Religion_Spirituality_Judaism      = ic("Religion & Spirituality","Judaism")
  val Religion_Spirituality_Religion     = ic("Religion & Spirituality","Religion")
  val Religion_Spirituality_Spirituality = ic("Religion & Spirituality","Spirituality")

  val Science                  = ic("Science")
  val Science_Astronomy        = ic("Science","Astronomy")
  val Science_Chemistry        = ic("Science","Chemistry")
  val Science_Earth_Sciences   = ic("Science","Earth Sciences")
  val Science_Life_Sciences    = ic("Science","Life Sciences")
  val Science_Mathematics      = ic("Science","Mathematics")
  val Science_Natural_Sciences = ic("Science","Natural Sciences")
  val Science_Nature           = ic("Science","Nature")
  val Science_Physics          = ic("Science","Physics")
  val Science_Social_Sciences  = ic("Science","Social Sciences")

  val Society_Culture                   = ic("Society & Culture")
  val Society_Culture_Documentary       = ic("Society & Culture","Documentary")
  val Society_Culture_Personal_Journals = ic("Society & Culture","Personal Journals")
  val Society_Culture_Philosophy        = ic("Society & Culture","Philosophy")
  val Society_Culture_Places_Travels    = ic("Society & Culture","Places & Travels")
  val Society_Culture_Relationships     = ic("Society & Culture","Relationships")

  val Sports                = ic("Sports")
  val Sports_Baseball       = ic("Sports","Baseball")
  val Sports_Basketball     = ic("Sports","Basketball")
  val Sports_Cricket        = ic("Sports","Cricket")
  val Sports_Fantasy_Sports = ic("Sports","Fantasy Sports")
  val Sports_Football       = ic("Sports","Football")
  val Sports_Golf           = ic("Sports","Golf")
  val Sports_Hockey         = ic("Sports","Hockey")
  val Sports_Rugby          = ic("Sports","Rugby")
  val Sports_Running        = ic("Sports","Running")
  val Sports_Soccer         = ic("Sports","Soccer")
  val Sports_Swimming       = ic("Sports","Swimming")
  val Sports_Tennis         = ic("Sports","Tennis")
  val Sports_Volleyball     = ic("Sports","Volleyball")
  val Sports_Wilderness     = ic("Sports","Wilderness")
  val Sports_Wrestling      = ic("Sports","Wrestling")

  val Technology = ic("Technology")

  val True_Crime = ic("True Crime")

  val TV_Film                 = ic("TV & Film")
  val TV_Film_After_Shows     = ic("TV & Film","After Shows")
  val TV_Film_Film_History    = ic("TV & Film","Film History")
  val TV_Film_Film_Interviews = ic("TV & Film","Film Interviews")
  val TV_Film_Film_Reviews    = ic("TV & Film","Film Reviews")
  val TV_Film_TV_Reviews      = ic("TV & Film","TV Reviews")






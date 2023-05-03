//
//  DetailScreen.swift
//  iosApp
//
//  Created by Josue Lubaki on 2023-05-03.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct DetailScreen: View {
    let movie : Movie
    var body: some View {
        ScrollView {
            VStack{
                ZStack{
                    AsyncImage(url: URL(string : movie.posterImage)){ image in
                        image.resizable().scaledToFill()
                    }
                    placeholder: {
                        ProgressView()
                    }
                }
                .frame(maxWidth: .infinity, minHeight: 300, maxHeight: 300)
                
                VStack(alignment: .leading, spacing: 12){
                    Text(movie.title)
                        .font(.title)
                        .fontWeight(.bold)
                        .fixedSize(horizontal: false, vertical: true)
                    
                    Button(action: {}){
                        HStack{
                            Image(systemName: "play.fill")
                                .foregroundColor(.white)
                            
                            Text("Start watching now")
                                .foregroundColor(.white)
                                .fontWeight(.bold)
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: 40)
                    .padding()
                    .background(.red)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    
                    Text("Released in \(movie.releaseDate)".uppercased())
                    
                    Text(movie.overview)
                        .font(.body)
                        .fixedSize(horizontal: false, vertical: true)

                }
                .padding(20)
                .background(.black)
                
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .navigationBarBackButtonHidden(true)
            .navigationBarItems(
                leading: CustomBackButton()
            )
        }
    }
}

private struct CustomBackButton: View {
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        Button(action: {
            presentationMode.wrappedValue.dismiss()
        }) {
            Image(systemName: "chevron.left")
                .foregroundColor(.white)
        }
    }
}

struct DetailScreen_Previews: PreviewProvider {
    static var previews: some View {
        DetailScreen(movie: sampleMovie)
    }
}

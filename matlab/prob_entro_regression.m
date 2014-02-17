
a = importdata('../dist-meeting-cases-u5000.txt');
features = a(:,1:2);
friendLabel = a(:,3);

fmean = repmat(min(features), size(features,1), 1);
fvariance = repmat(max(features-fmean), size(features,1), 1);
norm_features = (features - fmean) ./ fvariance;


mdl = LinearModel.fit(features, friendLabel)
